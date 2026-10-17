package org.example.booksharing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.booksharing.controller.EventController;
import org.example.booksharing.controller.NotificationController;
import org.example.booksharing.controller.TemplateController;
import org.example.booksharing.entities.*;
import org.example.booksharing.repository.*;
import org.example.booksharing.service.BookService;
import org.example.booksharing.service.FileService;
import org.example.booksharing.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** HTTP binding, validation, identity and ownership checks without an external database. */
class PersonalApiSecurityTest {
    private final UserRepository users = mock(UserRepository.class);
    private final BookRepository books = mock(BookRepository.class);
    private final EventRepository events = mock(EventRepository.class);
    private final NotificationRepository notifications = mock(NotificationRepository.class);
    private final TemplateRepository templates = mock(TemplateRepository.class);
    private final FileService files = mock(FileService.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final User owner = user(1L, "owner");
    private final User other = user(2L, "other");
    private LocalValidatorFactoryBean validator;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        UserService userService = new UserService(users, mock(ActionHistoryRepository.class), files);
        BookService bookService = new BookService(books, users, mock(RatingRepository.class), files);
        mvc = MockMvcBuilders.standaloneSetup(
                        new NotificationController(notifications, userService),
                        new EventController(events, userService, books),
                        new TemplateController(templates, mapper, userService, bookService, validator))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();
        when(users.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(events.save(any())).thenAnswer(call -> call.getArgument(0));
        when(templates.save(any())).thenAnswer(call -> call.getArgument(0));
        when(books.save(any())).thenAnswer(call -> {
            Book book = call.getArgument(0);
            book.setId(100L);
            book.prePersist();
            return book;
        });
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated("owner", null, List.of()));
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        validator.close();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/notifications", "/api/notifications"})
    void notificationRoutesUseAuthenticatedUser(String path) throws Exception {
        when(notifications.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(notification(10L, owner)));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$[0].userId").value(1));
        verify(notifications).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/notifications?userId=2", "/api/notifications?userId=2",
            "/events?user_id=2&date=2026-10-19", "/templates?userId=2"})
    void cannotSelectAnotherUsersData(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isForbidden());
        verifyNoInteractions(events, notifications, templates);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/notifications", "/events?date=2026-10-19", "/templates"})
    void missingAuthenticationFailsClosed(String path) throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get(path)).andExpect(status().isUnauthorized());
        verifyNoInteractions(events, notifications, templates);
    }

    @Test
    void anonymousAuthenticationCannotResolveAUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "test-key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        mvc.perform(get("/notifications")).andExpect(status().isUnauthorized());
        verify(users, never()).findByUsername("anonymousUser");
    }

    @Test
    void deletedUserCannotUsePersonalApi() throws Exception {
        when(users.findByUsername("owner")).thenReturn(Optional.empty());
        mvc.perform(get("/notifications")).andExpect(status().isUnauthorized());
        verifyNoInteractions(notifications);
    }

    @Test
    void mixedNotificationBatchDoesNotChangeAnyNotification() throws Exception {
        Notification own = notification(10L, owner);
        Notification foreign = notification(20L, other);
        when(notifications.findAllById(any())).thenReturn(List.of(own, foreign));
        mvc.perform(post("/notifications/mark_read").contentType(MediaType.APPLICATION_JSON).content("[10,20]"))
                .andExpect(status().isNotFound());
        assertFalse(own.isRead());
        assertFalse(foreign.isRead());
        verify(notifications, never()).saveAll(any());
    }

    @Test
    void missingNotificationAbortsEntireBatch() throws Exception {
        Notification own = notification(10L, owner);
        when(notifications.findAllById(any())).thenReturn(List.of(own));
        mvc.perform(post("/notifications/mark_read").contentType(MediaType.APPLICATION_JSON).content("[10,99]"))
                .andExpect(status().isNotFound());
        assertFalse(own.isRead());
        verify(notifications, never()).saveAll(any());
    }

    @Test
    void ownNotificationBatchSupportsDuplicatesAndRetries() throws Exception {
        Notification own = notification(10L, owner);
        when(notifications.findAllById(any())).thenReturn(List.of(own));
        for (int attempt = 0; attempt < 2; attempt++) {
            mvc.perform(post("/notifications/mark_read").contentType(MediaType.APPLICATION_JSON).content("[10,10]"))
                    .andExpect(status().isOk());
        }
        assertTrue(own.isRead());
        verify(notifications, times(2)).saveAll(List.of(own));
    }

    @ParameterizedTest
    @ValueSource(strings = {"[]", "[null]", "[0]", "[-1]", "null", "{}", "not-json"})
    void invalidNotificationBatchesReturn400(String body) throws Exception {
        mvc.perform(post("/notifications/mark_read").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(notifications);
    }

    @Test
    void oversizedNotificationBatchIsRejected() throws Exception {
        mvc.perform(post("/notifications/mark_read").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(java.util.Collections.nCopies(101, 10))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(notifications);
    }

    @Test
    void eventCreationRejectsForgedIdentity() throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(eventJson(2L, 10L)))
                .andExpect(status().isForbidden());
        verifyNoInteractions(events, books);
    }

    @Test
    void eventsCannotReferenceAnotherUsersHiddenBook() throws Exception {
        Book book = book(10L, other);
        book.setPublic(false);
        when(books.findById(10L)).thenReturn(Optional.of(book));
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(eventJson(1L, 10L)))
                .andExpect(status().isNotFound());
        book.setPublic(true);
        other.setPrivateBooks(true);
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(eventJson(1L, 10L)))
                .andExpect(status().isNotFound());
        verify(events, never()).save(any());
    }

    @Test
    void eventCreationReturnsOnlyExplicitFields() throws Exception {
        Book book = book(10L, other);
        book.setLikedUsers(List.of(other));
        when(books.findById(10L)).thenReturn(Optional.of(book));
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(eventJson(1L, 10L)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(10)).andExpect(jsonPath("$.user").doesNotExist())
                .andExpect(jsonPath("$.book").doesNotExist());
    }

    @Test
    void foreignEventsCannotBeEditedOrDeleted() throws Exception {
        Event event = event(other);
        when(events.findById(30L)).thenReturn(Optional.of(event));
        mvc.perform(put("/events/30").contentType(MediaType.APPLICATION_JSON).content(eventJson(1L, 10L)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/events/30")).andExpect(status().isNotFound());
        assertEquals("Original", event.getTitle());
        verify(events, never()).save(any());
        verify(events, never()).delete(any());
    }

    @Test
    void ownerCanEditAndDeleteEvent() throws Exception {
        Event event = event(owner);
        when(events.findById(30L)).thenReturn(Optional.of(event));
        mvc.perform(put("/events/30").contentType(MediaType.APPLICATION_JSON).content(eventJson(1L, 10L)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Reading"));
        mvc.perform(delete("/events/30")).andExpect(status().isOk());
        verify(events).delete(event);
    }

    @Test
    void eventBookCannotBeReassigned() throws Exception {
        Event event = event(owner);
        when(events.findById(30L)).thenReturn(Optional.of(event));
        mvc.perform(put("/events/30").contentType(MediaType.APPLICATION_JSON).content(eventJson(1L, 99L)))
                .andExpect(status().isBadRequest());
        assertEquals("Original", event.getTitle());
        verify(events, never()).save(any());
    }

    @Test
    void eventListIsScopedToCurrentUserAndRequestedDay() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 10, 19, 0, 0);
        when(events.findForDay(1L, start, start.plusDays(1))).thenReturn(List.of(event(owner)));
        mvc.perform(get("/events?date=2026-10-19")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(30)).andExpect(jsonPath("$[0].user").doesNotExist());
        verify(events).findForDay(1L, start, start.plusDays(1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/events?date=no-date", "/events", "/events?date=2026-10-19&user_id=bad"})
    void invalidEventQueryReturns400(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isBadRequest());
        verifyNoInteractions(events);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"title\":\"\",\"dateTime\":\"2026-10-19T12:00:00\"}",
            "{\"title\":\"Reading\",\"dateTime\":\"bad-date\"}"})
    void eventValidationRunsBeforePersistence(String body) throws Exception {
        mvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(events, books);
    }

    @Test
    void foreignTemplateCannotCreateBook() throws Exception {
        when(templates.findById(40L)).thenReturn(Optional.of(template(other, validBookJson())));
        mvc.perform(post("/templates/from_template/40")).andExpect(status().isNotFound());
        verifyNoInteractions(books);
    }

    @Test
    void templateCreationAndUseRejectForgedIdentity() throws Exception {
        mvc.perform(post("/templates").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("userId", 2, "name", "Novel", "data", validBookJson()))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/templates/from_template/40").contentType(MediaType.APPLICATION_JSON).content("{\"userId\":2}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(templates, books);
    }

    @Test
    void ownTemplatesDoNotExposeUserEntity() throws Exception {
        when(templates.findByUser(owner)).thenReturn(List.of(template(owner, validBookJson())));
        mvc.perform(get("/templates?userId=1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1)).andExpect(jsonPath("$[0].user").doesNotExist());
    }

    @Test
    void templateStorageDiscardsEntityFields() throws Exception {
        mvc.perform(post("/templates").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("name", "Novel", "data", hostileBookJson()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(1));
        ArgumentCaptor<Template> saved = ArgumentCaptor.forClass(Template.class);
        verify(templates).save(saved.capture());
        var data = mapper.readTree(saved.getValue().getData());
        assertFalse(data.has("id"));
        assertFalse(data.has("user"));
        assertFalse(data.has("likedUsers"));
        assertFalse(data.has("imageUrl"));
        assertFalse(data.has("qrCode"));
        assertFalse(data.get("isPublic").asBoolean());
    }

    @Test
    void legacyTemplateCreatesFreshBookThroughValidatedService() throws Exception {
        when(templates.findById(40L)).thenReturn(Optional.of(template(owner, hostileBookJson())));
        mvc.perform(post("/templates/from_template/40")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(books).save(saved.capture());
        Book book = saved.getValue();
        assertSame(owner, book.getUser());
        assertFalse(book.getPublic());
        assertEquals(0L, book.getLikesCount());
        assertEquals(0, book.getViews());
        assertNull(book.getImageUrl());
        assertNull(book.getFileUrl());
        assertNull(book.getLikedUsers());
        assertNotEquals("forged", book.getQrCode());
        assertNotNull(book.getQrCode());
        verifyNoInteractions(files);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[]", "{}", "not-json", "{\"title\":\"Title\",\"author\":\"Author\",\"year\":0}"})
    void invalidTemplateDataCannotBeStoredOrUsed(String data) throws Exception {
        mvc.perform(post("/templates").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("name", "Novel", "data", data))))
                .andExpect(status().isBadRequest());
        when(templates.findById(40L)).thenReturn(Optional.of(template(owner, data)));
        mvc.perform(post("/templates/from_template/40")).andExpect(status().isBadRequest());
        verify(templates, never()).save(any());
        verifyNoInteractions(books);
    }

    private static User user(long id, String name) {
        User user = new User(); user.setId(id); user.setUsername(name); user.setEmail(name + "@private.test");
        return user;
    }

    private static Book book(long id, User owner) {
        Book book = new Book(); book.setId(id); book.setTitle("Book"); book.setUser(owner); return book;
    }

    private static Notification notification(long id, User owner) {
        Notification notification = new Notification(); notification.setId(id); notification.setUserId(owner.getId());
        return notification;
    }

    private static Event event(User owner) {
        Event event = new Event(); event.setId(30L); event.setUser(owner); event.setBook(book(10L, owner));
        event.setTitle("Original"); event.setDateTime(LocalDateTime.of(2026, 10, 19, 12, 0)); return event;
    }

    private static Template template(User owner, String data) {
        Template template = new Template(); template.setId(40L); template.setUser(owner);
        template.setName("Novel"); template.setData(data); return template;
    }

    private String eventJson(Long userId, Long bookId) throws Exception {
        return mapper.writeValueAsString(Map.of("userId", userId, "bookId", bookId,
                "title", "Reading", "dateTime", "2026-10-19T12:00:00"));
    }

    private static String validBookJson() {
        return "{\"title\":\"Title\",\"author\":\"Author\",\"year\":2020}";
    }

    private static String hostileBookJson() {
        return """
                {"title":"Title","author":"Author","year":2020,"public":false,
                 "id":999,"user":{"id":2},"likedUsers":[{"id":2}],"likesCount":999,
                 "views":999,"qrCode":"forged","imageUrl":"/uploads/secret.png","fileUrl":"/uploads/secret.pdf"}
                """;
    }
}

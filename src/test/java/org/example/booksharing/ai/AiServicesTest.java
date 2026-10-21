package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.example.booksharing.entities.*;
import org.example.booksharing.repository.*;
import org.example.booksharing.service.RecommendationService;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class AiServicesTest {
    final AiProperties config = new AiProperties();
    final AiCatalog catalog = mock(AiCatalog.class);
    final EmbeddingIndex index = mock(EmbeddingIndex.class);
    final AiProvider provider = mock(AiProvider.class);
    final AiSearchService search = new AiSearchService(catalog, index, provider, config);
    final ObjectMapper json = new ObjectMapper().findAndRegisterModules();

    static Book book(long id, String description, String category) {
        Book b = new Book(); b.setId(id); b.setTitle("Книга " + id); b.setAuthor("Автор " + id);
        b.setDescription(description); b.setCategory(category); b.setTags(List.of());
        User owner = new User(); owner.setId(100L); owner.setUsername("owner"); b.setUser(owner);
        return b;
    }
    @Test void semanticSearchFindsWithoutKeywordAndFallbackIsExplicit() {
        config.setEnabled(true);
        Book a = book(1, "Размышления об одиночестве", "Классика"), b = book(2, "Учебник геометрии", "Наука");
        when(catalog.available()).thenReturn(List.of(a, b));
        when(index.current(anyList())).thenReturn(Map.of(1L, new double[]{1,0}, 2L, new double[]{0,1}));
        when(provider.embed(anyString())).thenReturn(new double[]{1,0});
        var result = search.search("Грустное и философское", null);
        assertEquals("hybrid", result.mode());
        assertEquals(List.of(1L), result.items().stream().map(h -> h.book().getId()).toList());
        when(provider.embed(anyString())).thenThrow(new AiUnavailableException());
        result = search.search("геометрии", null);
        assertEquals("keyword", result.mode()); assertEquals(2L, result.items().getFirst().book().getId());
        assertTrue(search.search("мореплавание", null).items().isEmpty());
    }
    @Test void similarNeverCallsModelOrReturnsSource() {
        Book a = book(1, "Космос", "Фантастика"), b = book(2, "Планеты", "Фантастика");
        when(catalog.publicBook(1)).thenReturn(a);
        when(catalog.available()).thenReturn(List.of(a,b));
        when(index.current(anyList())).thenReturn(Map.of());
        assertEquals(List.of(2L), search.similar(1).items().stream().map(h -> h.book().getId()).toList());
        verifyNoInteractions(provider);
    }
    @Test void staleAndWrongModelVectorsAreIgnored() {
        var repo = mock(BookEmbeddingRepository.class);
        var embeddingIndex = new EmbeddingIndex(repo, mock(BookRepository.class), provider, config, json,
                mock(PlatformTransactionManager.class));
        Book a = book(1, "Исходное описание", "Наука");
        BookEmbedding vector = new BookEmbedding(); vector.setBookId(1L);
        vector.setContentHash(BookText.hash(BookText.of(a))); vector.setModelKey(config.modelKey());
        vector.setVectorJson("[1,0]");
        when(repo.findAllById(any())).thenReturn(List.of(vector));
        assertEquals(1, embeddingIndex.current(List.of(a)).size());
        a.setDescription("Изменённое описание");
        assertTrue(embeddingIndex.current(List.of(a)).isEmpty());
        vector.setContentHash(BookText.hash(BookText.of(a))); vector.setModelKey("other-model");
        assertTrue(embeddingIndex.current(List.of(a)).isEmpty());
    }
    @Test void structuredChatRejectsInventedIdsAndEvidence() throws Exception {
        config.setEnabled(true);
        var retrieval = mock(AiSearchService.class);
        var verify = mock(AiResultVerifier.class);
        var hit = AiSearchService.hit(book(1, "Роман об одиночестве.", "Классика"), .8, "По теме");
        when(retrieval.search(anyString(), isNull())).thenReturn(new AiDtos.Results(List.of(hit), "hybrid", ""));
        when(verify.stillAvailable(anyList())).thenAnswer(c -> c.getArgument(0));
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var assistant = new AiAssistantService(retrieval, mock(RecommendationService.class), provider, config,
                    json, factory.getValidator(), verify);
            var input = new AiDtos.Chat("Посоветуй роман", List.of(), false);
            for (String invalid : List.of("{\"books\":[{\"id\":999,\"evidence\":\"\"}]}",
                    "{\"books\":[{\"id\":1,\"evidence\":\"Вымышленный сюжет\"}]}", "not json")) {
                when(provider.complete(anyString(), anyString())).thenReturn(invalid);
                var result = assistant.chat(input, new User());
                assertEquals("retrieval", result.mode());
                assertEquals(1L, result.items().getFirst().book().getId());
                assertFalse(result.items().getFirst().explanation().contains("Вымышленный"));
            }
            when(provider.complete(anyString(), anyString())).thenReturn(
                    "{\"books\":[{\"id\":1,\"evidence\":\"об одиночестве\"}],\"followUp\":\"genre\"}");
            var result = assistant.chat(input, new User());
            assertEquals("grounded", result.mode());
            assertTrue(result.items().getFirst().explanation().contains("об одиночестве"));
            var payload = org.mockito.ArgumentCaptor.forClass(String.class);
            verify(provider, atLeastOnce()).complete(anyString(),payload.capture());
            var context = json.readTree(payload.getValue()).path("context").get(0);
            assertFalse(context.has("username"));
            assertFalse(context.has("userId"));
            assertFalse(context.has("password"));
        }
    }
    @Test void descriptionNeverSavesAndRejectsMalformedOutput() {
        config.setEnabled(true);
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var assistant = new AiAssistantService(search, mock(RecommendationService.class), provider, config,
                    json, factory.getValidator(), mock(AiResultVerifier.class));
            var input = new AiDtos.Description("Название", "Автор", "Достоверное описание.", "Наука", List.of());
            when(provider.complete(anyString(), anyString())).thenReturn("{\"description\":\"Текст\",\"tags\":17}");
            assertThrows(AiUnavailableException.class, () -> assistant.describe(input));
            when(provider.complete(anyString(), anyString())).thenReturn(
                    "{\"description\":\"Достоверное описание.\",\"tags\":[\"наука\"],\"category\":\"Наука\"}");
            assertTrue(assistant.describe(input).draft());
            verifyNoInteractions(catalog, index);
        }
    }
    @Test void recommendationsUseConsentAndNeverShowOwnedOrKnownBooks() {
        User user = new User(); user.setId(1L); user.setUsername("reader");
        var prefs = mock(AiPreferencesService.class);
        var books = mock(BookRepository.class);
        var fav = mock(FavoriteRepository.class);
        var ratings = mock(RatingRepository.class);
        var exchanges = mock(ExchangeRepository.class);
        Book favorite = book(2,"Космос","Фантастика"), candidate = book(3,"Планеты","Фантастика"),
                own = book(4,"Космос","Фантастика"); own.setUser(user);
        when(catalog.available()).thenReturn(List.of(favorite,candidate,own));
        when(books.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of(favorite,candidate,own));
        when(index.current(anyList())).thenReturn(Map.of());
        when(prefs.get(user)).thenReturn(new AiDtos.Preferences(List.of("Фантастика"), "", false));
        var rec = new RecommendationService(catalog,index,prefs,books,fav,ratings,exchanges);
        assertTrue(rec.recommend(user).items().stream().noneMatch(h -> h.book().getId() == 4));
        verifyNoInteractions(fav,ratings,exchanges);
        when(prefs.get(user)).thenReturn(new AiDtos.Preferences(List.of("Фантастика"), "", true));
        when(books.findFavoritesByUsername("reader")).thenReturn(List.of(favorite));
        assertEquals(List.of(3L), rec.recommend(user).items().stream().map(h -> h.book().getId()).toList());
    }
    @Test void rateLimitUsesRemoteAddressAndReturnsRetryAfter() throws Exception {
        config.setRequestsPerMinute(1);
        var limiter = new AiRateLimitInterceptor(config,json);
        var request = new MockHttpServletRequest("POST","/ai/search");
        request.setRemoteAddr("127.0.0.1");
        assertTrue(limiter.preHandle(request,new MockHttpServletResponse(),new Object()));
        request.addHeader("X-Forwarded-For","spoofed");
        var response = new MockHttpServletResponse();
        assertFalse(limiter.preHandle(request,response,new Object()));
        assertEquals(429,response.getStatus()); assertEquals("60",response.getHeader("Retry-After"));
    }
    @Test void cosineRejectsIncompatibleAndNonFiniteVectors() {
        assertEquals(0,BookText.cosine(new double[]{1},new double[]{1,0}));
        assertEquals(0,BookText.cosine(new double[]{Double.NaN},new double[]{1}));
        assertEquals(1,BookText.cosine(new double[]{1,0},new double[]{2,0}),1e-9);
    }
}

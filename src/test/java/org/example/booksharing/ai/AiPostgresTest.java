package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booksharing.entities.*;
import org.example.booksharing.repository.*;
import org.example.booksharing.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Opt-in, dedicated disposable PostgreSQL database only. Never point at the application DB. */
@EnabledIfEnvironmentVariable(named = "AI_TEST_DB_URL", matches = "jdbc:postgresql://[^/]+/booksharing_ai_test")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop", "ai.enabled=false", "ai.index-initial-delay-ms=86400000",
        "ai.requests-per-minute=120", "jwt.secret=ai-test-only-secret-at-least-thirty-two-bytes",
        "jwt.expiration=3600000", "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
@Transactional
class AiPostgresTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getenv("AI_TEST_DB_URL"));
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("AI_TEST_DB_USERNAME","ai_test"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AI_TEST_DB_PASSWORD",""));
    }
    @Autowired BookRepository books;
    @Autowired UserRepository users;
    @Autowired ExchangeRepository exchanges;
    @Autowired BookEmbeddingRepository vectors;
    @Autowired AiCatalog catalog;
    @Autowired AiSearchService search;
    @Autowired AiResultVerifier verifier;
    @Autowired EmbeddingIndex index;
    @Autowired AiProperties config;
    @Autowired AiPreferencesService preferences;
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired javax.sql.DataSource dataSource;
    @MockitoBean AiProvider provider;
    User owner, reader;
    @BeforeEach void setup() {
        owner = user("owner-" + UUID.randomUUID());
        reader = user("reader-" + UUID.randomUUID());
        config.setEnabled(false);
    }
    User user(String name) {
        User u = new User(); u.setUsername(name); u.setPassword("test-hash");
        return users.saveAndFlush(u);
    }
    Book book(String title, User user) {
        Book b = new Book(); b.setTitle(title); b.setAuthor("Автор"); b.setYear(2020);
        b.setDescription("Космос и путешествия"); b.setCategory("Фантастика");
        b.setTags(new ArrayList<>(List.of("космос"))); b.setUser(user);
        return books.saveAndFlush(b);
    }
    String token() { return "Bearer " + jwt.generateToken(reader.getUsername()); }

    @Test
    @org.springframework.transaction.annotation.Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    void migrationIsRepeatableAndForeignKeysCascade() throws Exception {
        String schema = "ai_migration_" + UUID.randomUUID().toString().replace("-", "");
        try (var connection = dataSource.getConnection(); var sql = connection.createStatement()) {
            sql.execute("CREATE SCHEMA " + schema);
            try {
                sql.execute("SET search_path TO " + schema);
                sql.execute("CREATE TABLE book (id bigint PRIMARY KEY)");
                sql.execute("CREATE TABLE users (id bigint PRIMARY KEY)");
                var script = new org.springframework.core.io.FileSystemResource("docs/sql/001_ai.sql");
                org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, script);
                org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(connection, script);
                sql.execute("INSERT INTO book VALUES (1)");
                sql.execute("INSERT INTO ai_book_embedding VALUES (1, 'hash', 'model', '[1,0]')");
                sql.execute("DELETE FROM book WHERE id=1");
                try (var result = sql.executeQuery("SELECT count(*) FROM ai_book_embedding")) {
                    assertTrue(result.next()); assertEquals(0,result.getInt(1));
                }
            } finally {
                sql.execute("SET search_path TO public");
                sql.execute("DROP SCHEMA " + schema + " CASCADE");
            }
        }
    }

    @Test void realQueryExcludesPrivateOwnersPrivateBooksAndBusyTransfers() {
        Book visible = book("Публичная",owner), hidden = book("Скрытая",owner), busy = book("Занятая",owner);
        hidden.setPublic(false); books.saveAndFlush(hidden);
        User privateOwner = user("private-" + UUID.randomUUID()); privateOwner.setPrivateBooks(true);
        users.saveAndFlush(privateOwner); book("Закрытая полка",privateOwner);
        Exchange exchange = new Exchange(); exchange.setRequester(reader); exchange.setOfferer(owner);
        exchange.setRequestedBook(busy); exchange.setStatus(Exchange.ExchangeStatus.IN_TRANSIT);
        exchanges.saveAndFlush(exchange);
        assertEquals(List.of(visible.getId()),catalog.available().stream().map(Book::getId).toList());
        assertThrows(org.example.booksharing.exception.ResourceNotFoundException.class,
                () -> search.similar(hidden.getId()));
        exchange.setStatus(Exchange.ExchangeStatus.CANCELLED); exchanges.saveAndFlush(exchange);
        assertEquals(2,catalog.available().size());
    }
    @Test void offeredBookIsAlsoUnavailableAndPendingDoesNotReserve() {
        Book requested = book("Запрошена",owner), offered = book("Предложена",reader);
        Exchange e = new Exchange(); e.setRequester(reader); e.setOfferer(owner);
        e.setRequestedBook(requested); e.setOfferedBook(offered);
        exchanges.saveAndFlush(e);
        assertEquals(2,catalog.available().size());
        e.setStatus(Exchange.ExchangeStatus.DELIVERED); exchanges.saveAndFlush(e);
        assertTrue(catalog.available().isEmpty());
    }
    @Test void chatRechecksPrivacyAndRejectsChangedEvidenceAfterGeneration() {
        Book b = book("Проверка контекста",owner);
        var hit = AiSearchService.hit(b,.8,"Обоснование");
        assertEquals(1,verifier.stillAvailable(List.of(hit)).size());
        b.setDescription("Изменилось во время генерации"); books.saveAndFlush(b);
        assertTrue(verifier.stillAvailable(List.of(hit)).isEmpty());
        var updatedHit = AiSearchService.hit(b,.8,"Обоснование");
        owner.setPrivateBooks(true); users.saveAndFlush(owner);
        assertTrue(verifier.stillAvailable(List.of(updatedHit)).isEmpty());
    }
    @Test void indexingUpdateModelChangeAndDeleteCascadeUsePostgres() {
        Book b = book("Индексируемая",owner);
        when(provider.embed(anyString())).thenReturn(new double[]{1,0,0});
        config.setEnabled(true);
        try {
            for (int i=0;i<3;i++) index.indexNextBatch();
            em.flush();
            assertEquals(1,index.current(List.of(b)).size());
            b.setDescription("Совершенно другое содержание"); books.saveAndFlush(b);
            assertTrue(index.current(List.of(b)).isEmpty());
            for (int i=0;i<3;i++) index.indexNextBatch();
            em.flush();
            assertEquals(1,index.current(List.of(b)).size());
            config.setEmbeddingModel("new-test-model");
            assertTrue(index.current(List.of(b)).isEmpty());
            for (int i=0;i<3;i++) index.indexNextBatch();
            em.flush();
            assertEquals(1,index.current(List.of(b)).size());
            Long id = b.getId();
            books.delete(b); em.flush(); em.clear();
            assertFalse(books.existsById(id));
            assertFalse(vectors.existsById(id));
        } finally {
            config.setEnabled(false); config.setEmbeddingModel("bge-m3");
        }
    }
    @Test void securityValidationPreferencesAndDisabledDescriptionOverHttp() throws Exception {
        book("Публичная",owner);
        mvc.perform(post("/ai/search").contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"космос\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.mode").value("keyword"));
        for (String url : List.of("/ai/preferences","/ai/recommendations"))
            mvc.perform(get(url)).andExpect(status().isUnauthorized());
        mvc.perform(post("/ai/chat").contentType(MediaType.APPLICATION_JSON)
                .content("{\"message\":\"космос\",\"history\":[]}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/ai/search").contentType(MediaType.APPLICATION_JSON)
                .content("{\"query\":\"" + "я".repeat(1001) + "\"}")).andExpect(status().isBadRequest());
        mvc.perform(put("/ai/preferences").header("Authorization",token()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"genres\":[\"Фантастика\"],\"interests\":\"космос\",\"useHistory\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.useHistory").value(true));
        em.flush(); em.clear();
        assertEquals("космос", preferences.get(reader).interests());
        mvc.perform(get("/users/" + owner.getId() + "/suggestions").header("Authorization",token()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/ai/books/description-suggestion").header("Authorization",token())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Название\",\"author\":\"Автор\",\"description\":\"Сведения\",\"tags\":[]}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
        verifyNoInteractions(provider);
    }
}

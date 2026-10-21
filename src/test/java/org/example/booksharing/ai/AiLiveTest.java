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
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Explicit opt-in acceptance test: real PostgreSQL, real Ollama, real HTTP security/controller stack. */
@EnabledIfEnvironmentVariable(named = "AI_LIVE_TEST", matches = "true")
@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop", "ai.enabled=true", "ai.provider=ollama",
        "ai.base-url=http://127.0.0.1:11434", "ai.timeout-seconds=120", "ai.daily-call-budget=100",
        "ai.index-initial-delay-ms=86400000", "ai.requests-per-minute=120",
        "jwt.secret=ai-live-test-only-secret-at-least-thirty-two-bytes", "spring.jpa.open-in-view=false"
})
@AutoConfigureMockMvc
@Transactional
class AiLiveTest {
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry) {
        String url = System.getenv("AI_TEST_DB_URL");
        if (url == null || !url.matches("jdbc:postgresql://[^/]+/booksharing_ai_test"))
            throw new IllegalStateException("A dedicated booksharing_ai_test database is required");
        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.username", () -> System.getenv().getOrDefault("AI_TEST_DB_USERNAME", "ai_test"));
        registry.add("spring.datasource.password", () -> System.getenv().getOrDefault("AI_TEST_DB_PASSWORD", ""));
    }
    @Autowired BookRepository books;
    @Autowired UserRepository users;
    @Autowired EmbeddingIndex index;
    @Autowired BookEmbeddingRepository vectors;
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;
    @Autowired ObjectMapper json;

    Book book(User owner, String title, String author, String category, String description) {
        Book b = new Book(); b.setUser(owner); b.setTitle(title); b.setAuthor(author);
        b.setCategory(category); b.setDescription(description); b.setYear(2020); b.setTags(new ArrayList<>());
        return books.saveAndFlush(b);
    }
    @Test void allFiveFeaturesWorkWithLocalModels() throws Exception {
        User owner = new User(); owner.setUsername("ai-live-owner"); owner.setPassword("test-only"); users.saveAndFlush(owner);
        User reader = new User(); reader.setUsername("ai-live-reader"); reader.setPassword("test-only"); users.saveAndFlush(reader);
        Book dystopia = book(owner,"1984","Джордж Оруэлл","Фантастика",
                "Антиутопический роман о тоталитарном государстве, слежке, цензуре и контроле мыслей. Герой пытается сохранить личную свободу.");
        Book alternative = book(owner,"О дивный новый мир","Олдос Хаксли","Фантастика",
                "Антиутопия об обществе будущего, где государство контролирует людей. Роман поднимает вопросы свободы личности и социального контроля.");
        Book programming = book(owner,"Python для начинающих","Учебный автор","Наука",
                "Учебник программирования на Python для новичков. Пошаговые объяснения переменных, циклов и функций. Практические примеры и упражнения.");
        book(owner,"Хоббит","Джон Толкин","Фэнтези",
                "Фэнтезийная повесть о путешествии Бильбо Бэггинса с гномами, волшебнике Гэндальфе и драконе. Дружба, смелость и приключения.");
        book(owner,"Размышления об одиночестве","Тестовый автор","Классика",
                "Грустный философский роман о поиске смысла жизни. Внутренний конфликт, одиночество и размышления о человеческом существовании.");
        Book hidden = book(owner,"Скрытый учебник Python","Тестовый автор","Наука","Программирование Python для новичков.");
        hidden.setPublic(false); books.saveAndFlush(hidden);
        index.indexNextBatch();
        assertEquals(5,vectors.count(),"Every public fixture must have a real embedding");
        assertFalse(vectors.existsById(hidden.getId()));
        String token = "Bearer " + jwt.generateToken(reader.getUsername());
        Map<String,Object> report = new LinkedHashMap<>();

        String searchBody = mvc.perform(post("/ai/search").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("query","Хочу научиться писать программы с нуля"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.mode").value("hybrid"))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        var searchResult = json.readTree(searchBody);
        assertEquals(programming.getId(),searchResult.path("items").get(0).path("book").path("id").asLong());
        report.put("semanticSearch",searchResult);

        String similarBody = mvc.perform(get("/ai/books/" + dystopia.getId() + "/similar"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(alternative.getId(),json.readTree(similarBody).path("items").get(0).path("book").path("id").asLong());
        report.put("similar",json.readTree(similarBody));

        mvc.perform(put("/ai/preferences").header("Authorization",token).contentType(MediaType.APPLICATION_JSON)
                .content("{\"genres\":[\"Наука\"],\"interests\":\"программирование\",\"useHistory\":false}"))
                .andExpect(status().isOk());
        String recBody = mvc.perform(get("/ai/recommendations").header("Authorization",token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(programming.getId(),json.readTree(recBody).path("items").get(0).path("book").path("id").asLong());
        report.put("recommendations",json.readTree(recBody));

        String chatBody = mvc.perform(post("/ai/chat").header("Authorization",token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("message","Хочу научиться программировать. Посоветуй книгу для новичка.","history",List.of(),"usePreferences",true))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        var chat = json.readTree(chatBody);
        assertEquals("grounded",chat.path("mode").asText(),"The actual model must produce a valid grounded selection");
        assertFalse(chat.path("items").isEmpty());
        assertEquals(programming.getId(),chat.path("items").get(0).path("book").path("id").asLong());
        report.put("chat",chat);

        String draftBody = mvc.perform(post("/ai/books/description-suggestion").header("Authorization",token)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "title","Python для начинающих","author","Учебный автор","description",
                        "Это учебник Python для новичков. Есть примеры и упражнения. На обложке царапина.","category","Наука","tags",List.of("Python")))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.draft").value(true))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(json.readTree(draftBody).path("description").asText().contains("Python"));
        assertTrue(json.readTree(draftBody).path("description").asText().toLowerCase(Locale.ROOT).contains("царап"),
                "The draft must preserve the disclosed condition of the physical copy");
        report.put("descriptionDraft",json.readTree(draftBody));
        assertEquals(6,books.count(),"Generation must not create or mutate catalog records");
        Files.createDirectories(Path.of("target"));
        json.writerWithDefaultPrettyPrinter().writeValue(Path.of("target/ai-live-results.json").toFile(),report);
    }
}

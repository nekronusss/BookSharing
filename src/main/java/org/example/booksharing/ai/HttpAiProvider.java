package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.*;
import java.util.*;
import java.util.concurrent.Semaphore;

@Component
public class HttpAiProvider implements AiProvider {
    private final AiProperties config;
    private final ObjectMapper json;
    private final WebClient client;
    private final Semaphore concurrent = new Semaphore(2);
    private LocalDate budgetDate = LocalDate.now(ZoneOffset.UTC);
    private int calls;
    private volatile Instant retryAfter = Instant.MIN;

    public HttpAiProvider(AiProperties config, ObjectMapper json) {
        this.config = config;
        this.json = json;
        this.client = WebClient.builder().baseUrl(config.getBaseUrl().replaceAll("/+$", ""))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(1024 * 1024)).build();
    }
    private synchronized void reserve() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        if (!budgetDate.equals(today)) { budgetDate = today; calls = 0; }
        if (!config.isEnabled() || Instant.now().isBefore(retryAfter)
                || calls >= config.getDailyCallBudget()) throw new AiUnavailableException();
        calls++;
    }
    private JsonNode post(String path, Object body) {
        if (!concurrent.tryAcquire()) throw new AiUnavailableException();
        try {
            reserve();
            var request = client.post().uri(path).contentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (!config.getApiKey().isBlank()) request.headers(h -> h.setBearerAuth(config.getApiKey()));
            String response = request.bodyValue(body).retrieve().bodyToMono(String.class)
                    .block(Duration.ofSeconds(config.getTimeoutSeconds()));
            if (response == null) throw new AiUnavailableException();
            return json.readTree(response);
        } catch (Exception e) {
            // Never propagate provider bodies, credentials or user text to logs or API errors.
            retryAfter = Instant.now().plusSeconds(30);
            throw new AiUnavailableException();
        } finally { concurrent.release(); }
    }
    @Override public double[] embed(String text) {
        boolean ollama = config.getProvider().equals("ollama");
        JsonNode response = post(ollama ? "/api/embed" : "/embeddings",
                Map.of("model", config.getEmbeddingModel(), "input", text));
        JsonNode vector = ollama ? response.path("embeddings").path(0)
                : response.path("data").path(0).path("embedding");
        if (!vector.isArray() || vector.isEmpty() || vector.size() > 8192) throw new AiUnavailableException();
        double[] result = new double[vector.size()];
        double norm = 0;
        for (int i = 0; i < result.length; i++) {
            if (!vector.get(i).isNumber()) throw new AiUnavailableException();
            result[i] = vector.get(i).asDouble();
            if (!Double.isFinite(result[i])) throw new AiUnavailableException();
            norm += result[i] * result[i];
        }
        if (!Double.isFinite(norm) || norm == 0) throw new AiUnavailableException();
        return result;
    }
    @Override public String complete(String system, String input) {
        boolean ollama = config.getProvider().equals("ollama");
        var body = new HashMap<String, Object>();
        body.put("model", config.getChatModel());
        body.put("messages", List.of(Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", input)));
        body.put("stream", false);
        if (ollama) {
            body.put("format", "json");
            body.put("options", Map.of("temperature", 0.1, "num_predict", 1200, "num_ctx", 8192));
        } else {
            body.put("response_format", Map.of("type", "json_object"));
            body.put("temperature", 0.1);
            body.put("max_tokens", 1200);
        }
        JsonNode response = post(ollama ? "/api/chat" : "/chat/completions", body);
        JsonNode content = ollama ? response.path("message").path("content")
                : response.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank() || content.asText().length() > 12000)
            throw new AiUnavailableException();
        return content.asText();
    }
}

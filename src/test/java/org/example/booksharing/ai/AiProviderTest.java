package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class AiProviderTest {
    HttpServer server;
    final AtomicReference<String> reply = new AtomicReference<>("{\"embeddings\":[[1,0,0]]}");
    final AtomicReference<String> path = new AtomicReference<>(), requestBody = new AtomicReference<>();
    final AtomicInteger calls = new AtomicInteger();
    final AtomicInteger delayMillis = new AtomicInteger();
    AiProperties config;
    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
        server.createContext("/", exchange -> {
            calls.incrementAndGet(); path.set(exchange.getRequestURI().getPath());
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
            try { Thread.sleep(delayMillis.get()); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            byte[] bytes = reply.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type","application/json");
            exchange.sendResponseHeaders(200,bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        server.start();
        config = new AiProperties(); config.setEnabled(true);
        config.setBaseUrl("http://127.0.0.1:" + server.getAddress().getPort());
    }
    @AfterEach void stop() { server.stop(0); }
    @Test void ollamaAndCompatibleContractsUseActualHttp() {
        var provider = new HttpAiProvider(config,new ObjectMapper());
        assertArrayEquals(new double[]{1,0,0}, provider.embed("Космос"));
        assertEquals("/api/embed",path.get());
        reply.set("{\"message\":{\"content\":\"{\\\"books\\\":[]}\"}}");
        assertEquals("{\"books\":[]}",provider.complete("rules","request"));
        assertTrue(requestBody.get().contains("\"stream\":false"));
        config.setProvider("openai-compatible");
        config.setBaseUrl(config.getBaseUrl()+"/v1");
        provider = new HttpAiProvider(config,new ObjectMapper());
        reply.set("{\"data\":[{\"embedding\":[0,1]}]}");
        assertArrayEquals(new double[]{0,1},provider.embed("Фантастика"));
        assertEquals("/v1/embeddings",path.get());
        reply.set("{\"choices\":[{\"message\":{\"content\":\"{}\"}}]}");
        assertEquals("{}",provider.complete("rules","request"));
        assertEquals("/v1/chat/completions",path.get());
    }
    @Test void budgetAndDisabledModeDoNotCallNetwork() {
        config.setDailyCallBudget(1);
        var provider = new HttpAiProvider(config,new ObjectMapper());
        provider.embed("first");
        assertThrows(AiUnavailableException.class, () -> provider.embed("second"));
        assertEquals(1,calls.get());
        config.setEnabled(false);
        assertThrows(AiUnavailableException.class, () -> provider.complete("rules","request"));
        assertEquals(1,calls.get());
    }
    @Test void invalidEmbeddingCannotEnterTheIndex() {
        var provider = new HttpAiProvider(config,new ObjectMapper());
        for (String body : java.util.List.of("{\"embeddings\":[[0,0]]}", "{\"embeddings\":[[\"bad\",1]]}", "{}")) {
            reply.set(body);
            assertThrows(AiUnavailableException.class, () -> provider.embed("request"));
        }
    }
    @Test void timeoutAndCircuitBreakerBoundWaiting() {
        config.setTimeoutSeconds(1);
        delayMillis.set(3000);
        var provider = new HttpAiProvider(config,new ObjectMapper());
        long start = System.nanoTime();
        assertThrows(AiUnavailableException.class, () -> provider.embed("request"));
        assertTrue(java.time.Duration.ofNanos(System.nanoTime()-start).toMillis() < 2500);
        assertThrows(AiUnavailableException.class, () -> provider.embed("retry"));
        assertEquals(1,calls.get());
    }
}

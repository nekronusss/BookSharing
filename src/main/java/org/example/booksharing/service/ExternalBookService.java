package org.example.booksharing.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ExternalBookService {

    private final WebClient webClient;

    public ExternalBookService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com/books/v1")
                .build();
    }

    public JsonNode searchBooks(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/volumes")
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}

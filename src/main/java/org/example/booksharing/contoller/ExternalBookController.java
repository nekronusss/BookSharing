package org.example.booksharing.contoller;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.booksharing.service.ExternalBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external/books")
public class ExternalBookController {

    private final ExternalBookService externalBookService;

    public ExternalBookController(ExternalBookService externalBookService) {
        this.externalBookService = externalBookService;
    }

    @GetMapping
    public ResponseEntity<JsonNode> searchExternal(@RequestParam String query) {
        JsonNode result = externalBookService.searchBooks(query);
        return ResponseEntity.ok(result);
    }
}

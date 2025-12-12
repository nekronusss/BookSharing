package org.example.booksharing.contoller;

import org.example.booksharing.entities.Book;
import org.example.booksharing.service.RecommendationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) { this.service = service; }

    @GetMapping("/{id}/suggestions")
    public List<Book> get(@PathVariable Long id) {
        return service.getRecommendations(id);
    }
}

package org.example.booksharing.controller;

import org.example.booksharing.dto.BookDto;
import org.example.booksharing.service.UserService;
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
    private final UserService users;

    public RecommendationController(RecommendationService service, UserService users) {
        this.service = service; this.users = users;
    }

    @GetMapping("/{id}/suggestions")
    public List<BookDto> get(@PathVariable Long id) {
        return service.recommend(users.requireCurrentUser(id)).items().stream()
                .map(org.example.booksharing.ai.AiDtos.Hit::book).toList();
    }
}

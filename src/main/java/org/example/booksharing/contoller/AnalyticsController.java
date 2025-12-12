package org.example.booksharing.contoller;

import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.CommentRepository;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final AnalyticsService service;


    public AnalyticsController(BookRepository bookRepository, UserRepository userRepository, CommentRepository commentRepository, AnalyticsService service) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.service = service;
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalBooks", bookRepository.count());
        data.put("totalUsers", userRepository.count());
        data.put("totalComments", commentRepository.count());
        data.put("averageRating", bookRepository.getAverageRating());
        data.put("topUsers", userRepository.findTopUsers());
        return data;
    }

    @GetMapping("/users/{id}")
    public Map<String,Object> userStats(@PathVariable Long id) {
        return service.getUserStats(id);
    }

    @GetMapping
    public Map<String,Object> globalStats() {
        return service.getGlobalStats();
    }
}



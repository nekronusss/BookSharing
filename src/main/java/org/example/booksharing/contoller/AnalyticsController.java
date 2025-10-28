package org.example.booksharing.contoller;

import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.CommentRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
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

    public AnalyticsController(BookRepository bookRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
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
}


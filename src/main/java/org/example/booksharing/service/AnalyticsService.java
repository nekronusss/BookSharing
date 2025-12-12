package org.example.booksharing.service;

import org.example.booksharing.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AnalyticsService {
    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final CommentRepository commentRepo;
    private final RatingRepository ratingRepo;
    private final FollowRepository followRepo;

    public AnalyticsService(UserRepository userRepo, BookRepository bookRepo,
                            CommentRepository commentRepo, RatingRepository ratingRepo,
                            FollowRepository followRepo) {
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
        this.commentRepo = commentRepo;
        this.ratingRepo = ratingRepo;
        this.followRepo = followRepo;
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("books", bookRepo.findAll().stream().filter(b -> b.getUser().getId().equals(userId)).count());
        stats.put("comments", commentRepo.findAll().stream().filter(c -> c.getAuthor().getId().equals(userId)).count());
        stats.put("ratings", ratingRepo.findAll().stream().filter(r -> r.getUser().getId().equals(userId)).count());
        stats.put("followers", followRepo.findByFollowingId(userId).size());
        stats.put("following", followRepo.findByFollowerId(userId).size());
        return stats;
    }

    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookRepo.count());
        stats.put("totalComments", commentRepo.count());
        stats.put("totalRatings", ratingRepo.count());
        stats.put("totalUsers", userRepo.count());
        return stats;
    }
}


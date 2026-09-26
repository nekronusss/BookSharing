package org.example.booksharing.service;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final CommentRepository commentRepo;
    private final RatingRepository ratingRepo;
    private final FollowRepository followRepo;

    @Transactional(readOnly = true)
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("books", bookRepo.countByUserId(userId));
        stats.put("comments", commentRepo.countByAuthorId(userId));
        stats.put("ratings", ratingRepo.countByUserId(userId));
        stats.put("followers", followRepo.countByFollowingId(userId));
        stats.put("following", followRepo.countByFollowerId(userId));
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBooks", bookRepo.count());
        stats.put("totalComments", commentRepo.count());
        stats.put("totalRatings", ratingRepo.count());
        stats.put("totalUsers", userRepo.count());
        stats.put("averageRating", bookRepo.getAverageRating());
        return stats;
    }
}


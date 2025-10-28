package org.example.booksharing.contoller;

import org.example.booksharing.entities.Rating;
import org.example.booksharing.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/books")
public class RatingController {
    private final RatingService ratingService;
    public RatingController(RatingService ratingService) { this.ratingService = ratingService; }

    @PostMapping("/{id}/rating")
    public ResponseEntity<Rating> rate(@PathVariable Long id, @RequestBody Map<String,Object> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Integer score = body.containsKey("score") ? (Integer) body.get("score") : null;
        Boolean liked = body.containsKey("liked") ? (Boolean) body.get("liked") : null;
        Rating r = ratingService.addOrUpdate(id, score, liked, username);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String,Object>> stats(@PathVariable Long id) {
        Long likes = ratingService.getLikes(id);
        Double avg = ratingService.getAvg(id);
        return ResponseEntity.ok(Map.of("likes", likes == null ? 0 : likes, "avgRating", avg == null ? 0.0 : avg));
    }

    @DeleteMapping("/{id}/rating")
    public ResponseEntity<String> remove(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ratingService.removeRating(id, username);
        return ResponseEntity.ok("Removed");
    }
}

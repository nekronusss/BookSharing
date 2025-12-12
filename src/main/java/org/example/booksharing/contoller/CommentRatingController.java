package org.example.booksharing.contoller;

import org.example.booksharing.entities.CommentRating;
import org.example.booksharing.service.CommentRatingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentRatingController {

    private final CommentRatingService service;

    public CommentRatingController(CommentRatingService service) { this.service = service; }

    @PostMapping("/{id}/like")
    public CommentRating like(@PathVariable Long id, @RequestParam String username) {
        return service.likeComment(id, username);
    }

    @PostMapping("/{id}/rating")
    public CommentRating rate(@PathVariable Long id, @RequestParam String username, @RequestParam int score) {
        return service.rateComment(id, username, score);
    }

    @GetMapping("/{id}/ratings")
    public List<CommentRating> getRatings(@PathVariable Long id) {
        return service.getRatingsForComment(id);
    }
}


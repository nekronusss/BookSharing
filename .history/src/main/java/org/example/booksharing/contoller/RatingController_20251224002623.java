package org.example.booksharing.contoller;

import jakarta.validation.Valid;
import org.example.booksharing.dto.BookStatsDto;
import org.example.booksharing.dto.CreateRatingRequest;
import org.example.booksharing.dto.RatingDto;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.entities.Rating;
import org.example.booksharing.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class RatingController {
    private final RatingService ratingService;
    
    public RatingController(RatingService ratingService) { 
        this.ratingService = ratingService; 
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingDto> rate(@PathVariable Long id, @Valid @RequestBody CreateRatingRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Rating rating = ratingService.addOrUpdate(id, request.getScore(), request.getLiked(), username);
        return ResponseEntity.ok(DtoMapper.toRatingDto(rating));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<BookStatsDto> stats(@PathVariable Long id) {
        Long likes = ratingService.getLikes(id);
        Double avg = ratingService.getAvg(id);
        return ResponseEntity.ok(new BookStatsDto(likes, avg));
    }

    @DeleteMapping("/{id}/rating")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ratingService.removeRating(id, username);
        return ResponseEntity.noContent().build();
    }
}

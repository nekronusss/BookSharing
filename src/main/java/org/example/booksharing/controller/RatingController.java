package org.example.booksharing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.BookStatsDto;
import org.example.booksharing.dto.CreateRatingRequest;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.dto.RatingDto;
import org.example.booksharing.entities.Rating;
import org.example.booksharing.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class RatingController {
    private final RatingService ratingService;

    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingDto> rate(@PathVariable Long id, @Valid @RequestBody CreateRatingRequest request) {
        Rating rating = ratingService.addOrUpdate(id, request.getScore(), request.getLiked());
        return ResponseEntity.ok(DtoMapper.toRatingDto(rating));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<BookStatsDto> stats(@PathVariable Long id) {
        return ResponseEntity.ok(new BookStatsDto(ratingService.getLikes(id), ratingService.getAvg(id)));
    }

    @DeleteMapping("/{id}/rating")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        ratingService.removeRating(id);
        return ResponseEntity.noContent().build();
    }
}

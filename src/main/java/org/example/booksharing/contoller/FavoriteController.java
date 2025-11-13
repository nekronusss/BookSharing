package org.example.booksharing.contoller;

import org.example.booksharing.entities.Book;
import org.example.booksharing.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{id}")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) { this.favoriteService = favoriteService; }

    @PostMapping("/favorites")
    public void addFavorite(@PathVariable Long id, @RequestParam Long bookId) {
        favoriteService.addFavorite(id, bookId);
    }

    @GetMapping("/favorites")
    public List<Book> getFavorites(@PathVariable Long id) {
        return favoriteService.getFavorites(id);
    }

    @GetMapping("/recommendations")
    public List<Book> getRecommendations(@PathVariable Long id) {
        return favoriteService.getRecommendations(id);
    }
}

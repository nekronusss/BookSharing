package org.example.booksharing.service;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Favorite;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.FavoriteRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class RecommendationService {
    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final FavoriteRepository favRepo;

    public RecommendationService(UserRepository userRepo, BookRepository bookRepo, FavoriteRepository favRepo) {
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
        this.favRepo = favRepo;
    }

    public List<Book> getRecommendations(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();

        List<Book> favorites = favRepo.findByUserId(userId).stream().map(Favorite::getBook).toList();

        List<Book> popular = bookRepo.findAll().stream()
                .sorted((a,b) -> Double.compare(b.getRating(), a.getRating()))
                .limit(10)
                .toList();

        Set<Book> result = new LinkedHashSet<>();
        result.addAll(favorites);
        result.addAll(popular);

        return result.stream().toList();
    }
}

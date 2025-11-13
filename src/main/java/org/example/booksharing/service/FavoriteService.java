package org.example.booksharing.service;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Favorite;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.FavoriteRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteRepository repo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public FavoriteService(FavoriteRepository repo, BookRepository bookRepo, UserRepository userRepo) {
        this.repo = repo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
    }

    public void addFavorite(Long userId, Long bookId) {
        if (repo.existsByUserIdAndBookId(userId, bookId)) return;
        Favorite f = new Favorite();
        f.setUser(userRepo.findById(userId).orElseThrow());
        f.setBook(bookRepo.findById(bookId).orElseThrow());
        repo.save(f);
    }

    public List<Book> getFavorites(Long userId) {
        return repo.findByUserId(userId).stream().map(Favorite::getBook).toList();
    }

    public List<Book> getRecommendations(Long userId) {
        return bookRepo.findAll().stream()
                .sorted((a,b) -> Double.compare(b.getRating(), a.getRating()))
                .limit(10)
                .toList();
    }
}

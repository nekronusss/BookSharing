package org.example.booksharing.service;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Rating;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.RatingRepository;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.repository.ActionHistoryRepository;
import org.example.booksharing.entities.ActionHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {
    private final RatingRepository ratingRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final ActionHistoryRepository historyRepo;

    public RatingService(RatingRepository ratingRepo,
                         BookRepository bookRepo,
                         UserRepository userRepo,
                         ActionHistoryRepository historyRepo) {
        this.ratingRepo = ratingRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
    }

    @Transactional
    public Rating addOrUpdate(Long bookId, Integer score, Boolean liked, String username) {
        Book book = bookRepo.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        User user = userRepo.findByUsername(username).orElseThrow();

        Rating r = ratingRepo.findByUserIdAndBookId(user.getId(), bookId).orElse(new Rating());
        r.setBook(book);
        r.setUser(user);
        if (score != null) r.setScore(score);
        if (liked != null) r.setLiked(liked);
        Rating saved = ratingRepo.save(r);

        ActionHistory h = new ActionHistory();
        h.setUserId(user.getId());
        h.setActionType("RATE_BOOK");
        h.setEntityType("BOOK");
        h.setEntityId(bookId);
        h.setDetails("score=" + score + ", liked=" + liked);
        historyRepo.save(h);

        return saved;
    }

    public Long getLikes(Long bookId) { return ratingRepo.countLikesByBookId(bookId); }
    public Double getAvg(Long bookId) { return ratingRepo.avgScoreByBookId(bookId); }

    @Transactional
    public void removeRating(Long bookId, String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        ratingRepo.findByUserIdAndBookId(user.getId(), bookId).ifPresent(r -> {
            ratingRepo.delete(r);
            ActionHistory h = new ActionHistory();
            h.setUserId(user.getId());
            h.setActionType("REMOVE_RATING");
            h.setEntityType("BOOK");
            h.setEntityId(bookId);
            h.setDetails("");
            historyRepo.save(h);
        });
    }
}

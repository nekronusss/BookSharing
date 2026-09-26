package org.example.booksharing.service;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.ActionHistory;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Rating;
import org.example.booksharing.entities.User;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.ActionHistoryRepository;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.RatingRepository;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.util.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final RatingRepository ratingRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final ActionHistoryRepository historyRepo;

    @Transactional
    public Rating addOrUpdate(Long bookId, Integer score, Boolean liked) {
        String username = SecurityUtils.getCurrentUsername();
        Book book = bookRepo.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        User user = userRepo.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Rating r = ratingRepo.findByUserIdAndBookId(user.getId(), bookId).orElse(new Rating());
        r.setBook(book);
        r.setUser(user);
        if (score != null) r.setScore(score);
        if (liked != null) r.setLiked(liked);
        Rating saved = ratingRepo.save(r);

        logAction(user.getId(), "RATE_BOOK", "BOOK", bookId, "score=" + score + ", liked=" + liked);
        return saved;
    }

    @Transactional(readOnly = true)
    public Long getLikes(Long bookId) { return ratingRepo.countLikesByBookId(bookId); }

    @Transactional(readOnly = true)
    public Double getAvg(Long bookId) { return ratingRepo.avgScoreByBookId(bookId); }

    @Transactional
    public void removeRating(Long bookId) {
        String username = SecurityUtils.getCurrentUsername();
        User user = userRepo.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        ratingRepo.findByUserIdAndBookId(user.getId(), bookId).ifPresent(r -> {
            ratingRepo.delete(r);
            logAction(user.getId(), "REMOVE_RATING", "BOOK", bookId, "");
        });
    }

    private void logAction(Long userId, String actionType, String entityType, Long entityId, String details) {
        ActionHistory h = new ActionHistory();
        h.setUserId(userId);
        h.setActionType(actionType);
        h.setEntityType(entityType);
        h.setEntityId(entityId);
        h.setDetails(details);
        historyRepo.save(h);
    }
}

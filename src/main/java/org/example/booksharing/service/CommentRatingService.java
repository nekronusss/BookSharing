package org.example.booksharing.service;

import jakarta.transaction.Transactional;
import org.example.booksharing.entities.Comment;
import org.example.booksharing.entities.CommentRating;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.CommentRatingRepository;
import org.example.booksharing.repository.CommentRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentRatingService {
    private final CommentRatingRepository repo;
    private final UserRepository userRepo;
    private final CommentRepository commentRepo;

    public CommentRatingService(CommentRatingRepository repo, UserRepository userRepo, CommentRepository commentRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.commentRepo = commentRepo;
    }

    @Transactional
    public CommentRating likeComment(Long commentId, String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        Comment comment = commentRepo.findById(commentId).orElseThrow();

        CommentRating cr = repo.findByUserIdAndCommentId(user.getId(), commentId)
                .orElse(new CommentRating());
        cr.setUser(user);
        cr.setComment(comment);
        cr.setLiked(true);

        return repo.save(cr);
    }

    @Transactional
    public CommentRating rateComment(Long commentId, String username, int score) {
        if(score < 1 || score > 5) throw new RuntimeException("Score must be 1-5");
        User user = userRepo.findByUsername(username).orElseThrow();
        Comment comment = commentRepo.findById(commentId).orElseThrow();

        CommentRating cr = repo.findByUserIdAndCommentId(user.getId(), commentId)
                .orElse(new CommentRating());
        cr.setUser(user);
        cr.setComment(comment);
        cr.setScore(score);

        return repo.save(cr);
    }

    public List<CommentRating> getRatingsForComment(Long commentId) {
        return repo.findByCommentId(commentId);
    }
}


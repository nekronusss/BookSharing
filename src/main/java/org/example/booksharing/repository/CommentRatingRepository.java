package org.example.booksharing.repository;

import org.example.booksharing.entities.CommentRating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface CommentRatingRepository extends JpaRepository<CommentRating, Long> {
    Optional<CommentRating> findByUserIdAndCommentId(Long userId, Long commentId);
    List<CommentRating> findByCommentId(Long commentId);
}

package org.example.booksharing.repository;

import org.example.booksharing.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.book.id = :bookId AND r.liked = true")
    Long countLikesByBookId(@Param("bookId") Long bookId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.book.id = :bookId AND r.score IS NOT NULL")
    Double avgScoreByBookId(@Param("bookId") Long bookId);
}

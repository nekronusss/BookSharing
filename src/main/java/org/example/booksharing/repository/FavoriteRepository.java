package org.example.booksharing.repository;

import org.example.booksharing.entities.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndBookId(Long userId, Long bookId);
    List<Favorite> findByUserId(Long userId);
}

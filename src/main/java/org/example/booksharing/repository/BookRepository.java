package org.example.booksharing.repository;

import org.example.booksharing.entities.Book;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Query("SELECT b FROM Book b WHERE LOWER(b.category) = LOWER(:category)")
    List<Book> findByCategory(@Param("category") String category);

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR EXISTS (SELECT t FROM b.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Book> search(@Param("search") String search);

    List<Book> findByRating(Double rating);

    @Query("SELECT b FROM Book b JOIN b.tags t WHERE LOWER(t) = LOWER(:tag)")
    List<Book> findByTag(@Param("tag") String tag);

    @Query("SELECT b FROM Book b JOIN b.likedUsers u WHERE u.username = :username")
    List<Book> findFavoritesByUsername(@Param("username") String username);

    @Query("SELECT AVG(b.rating) FROM Book b")
    Double getAverageRating();

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();

    @Query("SELECT b FROM Book b WHERE b.qrCode = :qrCode")
    java.util.Optional<Book> findByQrCode(@Param("qrCode") String qrCode);

    long countByUserId(Long userId);

    @NonNull
    List<Book> findAll(@Nullable Specification<Book> spec);

}
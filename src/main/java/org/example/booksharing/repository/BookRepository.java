package org.example.booksharing.repository;

import org.example.booksharing.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR EXISTS (SELECT t FROM b.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Book> search(@Param("search") String search);

    List<Book> findByRating(Double rating);

    @Query("SELECT b FROM Book b JOIN b.tags t WHERE LOWER(t) = LOWER(:tag)")
    List<Book> findByTag(@Param("tag") String tag);

    @Query("SELECT r.book FROM Rating r WHERE r.liked = true AND r.user.username = :username")
    List<Book> findFavoritesByUsername(@Param("username") String username);

    @Query("SELECT AVG(b.rating) FROM Book b")
    Double getAverageRating();

    @Query("SELECT b.category, COUNT(b) FROM Book b GROUP BY b.category")
    List<Object[]> countBooksByCategory();
}
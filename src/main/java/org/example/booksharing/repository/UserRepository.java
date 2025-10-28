package org.example.booksharing.repository;

import org.example.booksharing.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u.username, COUNT(b) as bookCount " +
            "FROM User u JOIN u.books b " +
            "GROUP BY u.username " +
            "ORDER BY bookCount DESC")
    List<Object[]> findTopUsers();

    @Query("SELECT u.username, COUNT(c) as commentCount " +
            "FROM User u JOIN u.comments c " +
            "GROUP BY u.username " +
            "ORDER BY commentCount DESC")
    List<Object[]> findMostActiveUsers();
}


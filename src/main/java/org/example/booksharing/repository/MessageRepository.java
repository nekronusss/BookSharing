package org.example.booksharing.repository;

import org.example.booksharing.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByBookIdOrderByTimestampAsc(Long bookId);
}


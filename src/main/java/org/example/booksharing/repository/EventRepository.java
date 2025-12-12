package org.example.booksharing.repository;

import org.example.booksharing.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserIdAndDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);
}


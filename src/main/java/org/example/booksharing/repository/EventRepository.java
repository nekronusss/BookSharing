package org.example.booksharing.repository;

import org.example.booksharing.entities.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByUserIdAndDateTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT e FROM Event e WHERE e.user.id = :userId AND e.dateTime >= :start AND e.dateTime < :end ORDER BY e.dateTime, e.id")
    List<Event> findForDay(@Param("userId") Long userId, @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}


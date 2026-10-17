package org.example.booksharing.dto;

import org.example.booksharing.entities.Event;

import java.time.LocalDateTime;

public record EventDto(Long id, String title, String description, Integer priority,
                       LocalDateTime dateTime, Long userId, Long bookId) {
    public static EventDto from(Event event) {
        return new EventDto(event.getId(), event.getTitle(), event.getDescription(), event.getPriority(),
                event.getDateTime(), event.getUser().getId(),
                event.getBook() == null ? null : event.getBook().getId());
    }
}

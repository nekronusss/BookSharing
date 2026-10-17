package org.example.booksharing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.EventDto;
import org.example.booksharing.dto.EventRequest;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Event;
import org.example.booksharing.entities.User;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.EventRepository;
import org.example.booksharing.security.ResourceAccess;
import org.example.booksharing.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final BookRepository bookRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody EventRequest request) {
        User user = userService.requireCurrentUser(request.userId());
        if (request.bookId() == null) throw new IllegalArgumentException("Book ID is required");
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        ResourceAccess.requireVisibleBook(book, user);

        Event event = new Event();
        apply(event, request);
        event.setUser(user);
        event.setBook(book);
        return ResponseEntity.ok(EventDto.from(eventRepository.save(event)));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<EventDto> getEventsByDate(
            @RequestParam(required = false) Long user_id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        User user = userService.requireCurrentUser(user_id);
        return eventRepository.findForDay(user.getId(), date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream().map(EventDto::from).toList();
    }

    @PutMapping("/{id}")
    @Transactional
    public EventDto updateEvent(@PathVariable Long id, @Valid @RequestBody EventRequest request) {
        User user = userService.requireCurrentUser(request.userId());
        Event event = ownedEvent(id, user);
        if (request.bookId() != null
                && (event.getBook() == null || !request.bookId().equals(event.getBook().getId()))) {
            throw new IllegalArgumentException("An event's book cannot be changed");
        }
        apply(event, request);
        return EventDto.from(eventRepository.save(event));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteEvent(@PathVariable Long id) {
        eventRepository.delete(ownedEvent(id, userService.getCurrentUser()));
    }

    private Event ownedEvent(Long id, User user) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        ResourceAccess.requireOwner(event.getUser(), user, "Event");
        return event;
    }

    private void apply(Event event, EventRequest request) {
        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setPriority(request.priority());
        event.setDateTime(request.dateTime());
    }
}

package org.example.booksharing.contoller;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Event;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.EventRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;


    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        Long bookId = Long.valueOf(payload.get("bookId").toString());

        User user = userRepository.findById(userId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Event event = new Event();
        event.setTitle((String) payload.get("title"));
        event.setDescription((String) payload.get("description"));
        event.setPriority((Integer) payload.get("priority"));
        event.setDateTime(LocalDateTime.parse((String) payload.get("dateTime")));
        event.setUser(user);
        event.setBook(book);

        Event saved = eventRepository.save(event);
        return ResponseEntity.ok(saved);
    }



    @GetMapping
    public List<Event> getEventsByDate(
            @RequestParam Long user_id,
            @RequestParam String date
    ) {
        LocalDate day = LocalDate.parse(date);

        return eventRepository.findByUserIdAndDateTimeBetween(
                user_id,
                day.atStartOfDay(),
                day.plusDays(1).atStartOfDay()
        );
    }

    @PutMapping("/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody Event updated) {
        Event event = eventRepository.findById(id).orElseThrow();
        event.setTitle(updated.getTitle());
        event.setDescription(updated.getDescription());
        event.setDateTime(updated.getDateTime());
        event.setPriority(updated.getPriority());
        return eventRepository.save(event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
    }
}

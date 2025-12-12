package org.example.booksharing.contoller;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.MessageRequest;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Message;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.MessageRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest req) {
        User sender = userRepository.findById(req.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setBook(book);
        message.setContent(req.getContent());
        message.setTimestamp(LocalDateTime.now());

        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Message> getMessagesByObject(@RequestParam Long object_id) {
        return messageRepository.findByBookIdOrderByTimestampAsc(object_id);
    }
}


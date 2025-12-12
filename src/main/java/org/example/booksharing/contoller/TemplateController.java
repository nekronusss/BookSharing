package org.example.booksharing.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.TemplateRequest;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Template;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.TemplateRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateRepository templateRepository;
    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @GetMapping
    public List<Template> getTemplates(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return templateRepository.findByUser(user);
    }


    @PostMapping
    public ResponseEntity<Template> createTemplate(@RequestBody TemplateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Template template = new Template();
        template.setName(request.getName());
        template.setData(request.getData());
        template.setUser(user);

        Template saved = templateRepository.save(template);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/from_template/{templateId}")
    public ResponseEntity<Book> createFromTemplate(
            @PathVariable Long templateId,
            @RequestBody Map<String, Long> body) throws Exception {

        Long userId = body.get("userId");

        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = objectMapper.readValue(template.getData(), Book.class);
        book.setId(null);
        book.setUser(user);
        book.setLikedUsers(new ArrayList<>());
        book.setFileUrl(null);
        book.setImageUrl(null);
        book.setLikesCount(0L);

        Book savedBook = bookRepository.save(book);
        return ResponseEntity.ok(savedBook);
    }
}

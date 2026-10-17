package org.example.booksharing.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.BookDto;
import org.example.booksharing.dto.CreateBookRequest;
import org.example.booksharing.dto.TemplateDto;
import org.example.booksharing.dto.TemplateRequest;
import org.example.booksharing.entities.Template;
import org.example.booksharing.entities.User;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.TemplateRepository;
import org.example.booksharing.security.ResourceAccess;
import org.example.booksharing.service.BookService;
import org.example.booksharing.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/templates")
@RequiredArgsConstructor
public class TemplateController {
    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final BookService bookService;
    private final Validator validator;

    @GetMapping
    @Transactional(readOnly = true)
    public List<TemplateDto> getTemplates(@RequestParam(required = false) Long userId) {
        return templateRepository.findByUser(userService.requireCurrentUser(userId))
                .stream().map(TemplateDto::from).toList();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<TemplateDto> createTemplate(@Valid @RequestBody TemplateRequest request)
            throws JsonProcessingException {
        User user = userService.requireCurrentUser(request.getUserId());
        CreateBookRequest book = parseBook(request.getData());
        Template template = new Template();
        template.setName(request.getName().trim());
        // Store only editable book fields, never entity IDs, relationships, URLs or counters.
        template.setData(objectMapper.writeValueAsString(book));
        template.setUser(user);
        return ResponseEntity.ok(TemplateDto.from(templateRepository.save(template)));
    }

    @PostMapping("/from_template/{templateId}")
    @Transactional
    public ResponseEntity<BookDto> createFromTemplate(@PathVariable Long templateId,
            @RequestBody(required = false) Map<String, Long> body) throws IOException {
        User user = userService.requireCurrentUser(body == null ? null : body.get("userId"));
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
        ResourceAccess.requireOwner(template.getUser(), user, "Template");
        return ResponseEntity.ok(bookService.addBook(parseBook(template.getData()), null));
    }

    private CreateBookRequest parseBook(String json) {
        if (json == null || json.length() > 20000) throw new IllegalArgumentException("Invalid template data");
        CreateBookRequest request;
        try {
            // Unknown legacy entity fields are discarded; they cannot reach BookService.
            request = objectMapper.readerFor(CreateBookRequest.class)
                    .without(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(json);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Template data must be a book JSON object");
        }
        if (request == null) throw new IllegalArgumentException("Template data must be a book JSON object");
        var errors = validator.validate(request);
        if (!errors.isEmpty()) throw new IllegalArgumentException(errors.iterator().next().getMessage());
        return request;
    }
}

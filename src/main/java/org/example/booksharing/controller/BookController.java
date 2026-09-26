package org.example.booksharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.BookDto;
import org.example.booksharing.dto.CreateBookRequest;
import org.example.booksharing.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<List<BookDto>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category
    ) {
        return ResponseEntity.ok(bookService.getBooks(search, rating, tag, category));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<BookDto> addBook(
            @RequestPart("book") String bookJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        CreateBookRequest request = objectMapper.readValue(bookJson, CreateBookRequest.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(request, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<BookDto> toggleLike(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.toggleLike(id));
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<BookDto>> getFavorites() {
        return ResponseEntity.ok(bookService.getFavorites());
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BookDto>> searchBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer ratingMin,
            @RequestParam(required = false) Integer ratingMax,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer viewsMin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime updatedAfter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(bookService.searchBooks(search, author, ratingMin, ratingMax, category, tag, viewsMin, updatedAfter, pageable));
    }
}

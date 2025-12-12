package org.example.booksharing.contoller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.BookSpecification;
import org.example.booksharing.repository.RatingRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    @Autowired
    public BookController(BookRepository bookRepository,
                          UserRepository userRepository,
                          RatingRepository ratingRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    private Book enrichBook(Book book) {
        if (book == null) return null;

        Double avg = ratingRepository.avgScoreByBookId(book.getId());
        book.setRating(avg != null ? avg : 0.0);

        Long likes = (long) (book.getLikedUsers() != null ? book.getLikedUsers().size() : 0);
        book.setLikesCount(likes);

        if (book.getLikedUsers() == null) {
            book.setLikedUsers(new ArrayList<>());
        }
        if (book.getFileUrl() == null) {
            book.setFileUrl("");
        }
        if (book.getImageUrl() == null) {
            book.setImageUrl("");
        }

        return book;
    }



    private List<Book> enrichBooks(List<Book> books) {
        return books.stream().map(this::enrichBook).toList();
    }

    @GetMapping
    public List<Book> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer viewsMin

    ) {
        List<Book> books;

        if (search != null && !search.isEmpty()) {
            books = bookRepository.search(search);
        } else if (rating != null) {
            books = bookRepository.findByRating(rating);
        } else if (tag != null && !tag.isEmpty()) {
            books = bookRepository.findByTag(tag);
        } else if (category != null && !category.isEmpty()) {
            books = bookRepository.findByCategory(category);
        } else {
            books = bookRepository.findAll();
        }

        return enrichBooks(books);
    }


    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> addBook(
            @RequestPart("book") String bookJson,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        Book book = mapper.readValue(bookJson, Book.class);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > 5 * 1024 * 1024)
                return ResponseEntity.badRequest().body("File too large (max 5MB)");

            String contentType = file.getContentType();
            if (!List.of("image/jpeg", "image/png", "application/pdf").contains(contentType))
                return ResponseEntity.badRequest().body("Invalid file type");

            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            file.transferTo(filePath.toFile());

            String fileUrl = "/uploads/" + fileName;
            if (contentType.startsWith("image")) book.setImageUrl(fileUrl);
            else book.setFileUrl(fileUrl);
        }

        book.setUser(user);
        bookRepository.save(book);
        return ResponseEntity.ok("Book added successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElseThrow();
        book.setActivityScore(book.getActivityScore() + 1);
        bookRepository.save(book);
        return ResponseEntity.ok(enrichBook(book));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        Book book = bookRepository.findById(id).orElseThrow();
        if (!book.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot delete someone else's book");
        }

        if (book.getFileUrl() != null && !book.getFileUrl().isEmpty()) {
            Path filePath = Paths.get(UPLOAD_DIR, new File(book.getFileUrl()).getName());
            try { Files.deleteIfExists(filePath); } catch (IOException ignored) {}
        }

        bookRepository.deleteById(id);
        return ResponseEntity.ok("Book deleted");
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Book book = bookRepository.findById(id).orElseThrow();

        if (book.getLikedUsers().contains(user)) {
            book.getLikedUsers().remove(user);
        } else {
            book.getLikedUsers().add(user);
        }

        book.setLikesCount((long) book.getLikedUsers().size());

        bookRepository.save(book);

        return ResponseEntity.ok(enrichBook(book));
    }



    @GetMapping("/favorites")
    public ResponseEntity<List<Book>> getFavorites() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Book> favorites = bookRepository.findFavoritesByUsername(username);
        return ResponseEntity.ok(enrichBooks(favorites));
    }

    @GetMapping("/search")
    public Page<Book> searchBooks(
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
        return bookRepository.findAll(
                BookSpecification.filter(search, author, ratingMin, ratingMax, category, tag, viewsMin, updatedAfter),
                pageable
        );
    }


}

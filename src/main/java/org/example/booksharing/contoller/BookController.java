package org.example.booksharing.contoller;

import org.example.booksharing.entities.User;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.entities.Book;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookController(BookRepository bookRepository,
                          UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    public Book getBookById(@PathVariable Long id) {
        return bookRepository.findById(id).orElseThrow();
    }

    @PostMapping
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User user = userRepository.findByUsername(username).orElseThrow();
        book.setUser(user);
        bookRepository.save(book);

        return ResponseEntity.ok("Book added by " + username);
    }


    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book updatedBook) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        return bookRepository.findById(id)
                .map(book -> {
                    if (!book.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("You cannot edit someone else's book");
                    }
                    book.setTitle(updatedBook.getTitle());
                    book.setAuthor(updatedBook.getAuthor());
                    book.setYear(updatedBook.getYear());
                    book.setDescription(updatedBook.getDescription());
                    book.setQrCode(updatedBook.getQrCode());
                    return bookRepository.save(book);
                })
                .orElseThrow(() -> new RuntimeException("Book not found: " + id));
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
        bookRepository.deleteById(id);
        return ResponseEntity.ok("Book deleted");
    }
}

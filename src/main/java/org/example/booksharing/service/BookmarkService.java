package org.example.booksharing.service;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Bookmark;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.BookmarkRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public Bookmark addBookmark(Long userId, Long bookId, LocalDateTime dueDate, Integer priority) {
        User user = userRepository.findById(userId).orElseThrow();
        Book book = bookRepository.findById(bookId).orElseThrow();

        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setBook(book);
        bookmark.setAddedAt(LocalDateTime.now());
        bookmark.setDueDate(dueDate);
        bookmark.setPriority(priority);

        return bookmarkRepository.save(bookmark);
    }

    public List<Bookmark> getUserBookmarks(Long userId) {
        return bookmarkRepository.findAllByUserId(userId);
    }

    public Bookmark updateBookmark(Long bookmarkId, LocalDateTime dueDate, Integer priority) {
        Bookmark bookmark = bookmarkRepository.findById(bookmarkId).orElseThrow();
        if (dueDate != null) bookmark.setDueDate(dueDate);
        if (priority != null) bookmark.setPriority(priority);
        return bookmarkRepository.save(bookmark);
    }
}

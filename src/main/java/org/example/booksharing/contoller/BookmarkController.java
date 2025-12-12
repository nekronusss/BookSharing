package org.example.booksharing.contoller;

import org.example.booksharing.entities.Bookmark;
import org.example.booksharing.service.BookmarkService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping
    public Bookmark addBookmark(@PathVariable Long userId,
                                @RequestParam Long bookId,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
                                @RequestParam(required = false) Integer priority) {
        return bookmarkService.addBookmark(userId, bookId, dueDate, priority);
    }

    @GetMapping
    public List<Bookmark> getBookmarks(@PathVariable Long userId) {
        return bookmarkService.getUserBookmarks(userId);
    }
}


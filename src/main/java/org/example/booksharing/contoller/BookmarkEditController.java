package org.example.booksharing.contoller;

import org.example.booksharing.entities.Bookmark;
import org.example.booksharing.service.BookmarkService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkEditController {

    private final BookmarkService bookmarkService;

    public BookmarkEditController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PutMapping("/{bookmarkId}")
    public Bookmark updateBookmark(@PathVariable Long bookmarkId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dueDate,
                                   @RequestParam(required = false) Integer priority) {
        return bookmarkService.updateBookmark(bookmarkId, dueDate, priority);
    }
}


package org.example.booksharing.service;

import org.example.booksharing.entities.*;
import org.example.booksharing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final ActionHistoryRepository historyRepo;
    private final NotificationRepository notificationRepo;

    public CommentService(CommentRepository commentRepo,
                          BookRepository bookRepo,
                          UserRepository userRepo,
                          ActionHistoryRepository historyRepo,
                          NotificationRepository notificationRepo) {
        this.commentRepo = commentRepo;
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    public Comment addComment(Long bookId, String text, String username) {
        Book book = bookRepo.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        User user = userRepo.findByUsername(username).orElseThrow();
        Comment c = new Comment();
        c.setBook(book);
        c.setAuthor(user);
        c.setText(text);
        Comment saved = commentRepo.save(c);

        ActionHistory h = new ActionHistory();
        h.setUserId(user.getId());
        h.setActionType("CREATE_COMMENT");
        h.setEntityType("COMMENT");
        h.setEntityId(saved.getId());
        h.setDetails("bookId=" + bookId);
        historyRepo.save(h);

        if (!book.getUser().getId().equals(user.getId())) {
            Notification n = new Notification();
            n.setUserId(book.getUser().getId());
            n.setType("COMMENT");
            n.setMessage(user.getUsername() + " commented on your book: " + text);
            notificationRepo.save(n);
        }

        return saved;
    }

    public List<Comment> getComments(Long bookId) {
        return commentRepo.findByBookIdOrderByCreatedAtAsc(bookId);
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment c = commentRepo.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        if (!c.getAuthor().getUsername().equals(username)) throw new RuntimeException("Forbidden");
        commentRepo.deleteById(commentId);

        ActionHistory h = new ActionHistory();
        h.setUserId(c.getAuthor().getId());
        h.setActionType("DELETE_COMMENT");
        h.setEntityType("COMMENT");
        h.setEntityId(commentId);
        h.setDetails("bookId=" + c.getBook().getId());
        historyRepo.save(h);
    }
}
package org.example.booksharing.contoller;

import org.example.booksharing.entities.Comment;
import org.example.booksharing.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class CommentController {
    private final CommentService commentService;
    public CommentController(CommentService commentService) { this.commentService = commentService; }

    @PostMapping("/books/{id}/comments")
    public ResponseEntity<Comment> add(@PathVariable Long id, @RequestBody Map<String,String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Comment c = commentService.addComment(id, body.get("text"), username);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/books/{id}/comments")
    public ResponseEntity<List<Comment>> list(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getComments(id));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.deleteComment(id, username);
        return ResponseEntity.ok("Deleted");
    }
}

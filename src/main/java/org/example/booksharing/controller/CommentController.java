package org.example.booksharing.controller;

import jakarta.validation.Valid;
import org.example.booksharing.dto.CommentDto;
import org.example.booksharing.dto.CreateCommentRequest;
import org.example.booksharing.dto.UpdateCommentRequest;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/books")
public class CommentController {
    private final CommentService commentService;
    
    public CommentController(CommentService commentService) { 
        this.commentService = commentService; 
    }

    @PostMapping("/{bookId}/comments")
    public ResponseEntity<CommentDto> add(@PathVariable Long bookId, @Valid @RequestBody CreateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var comment = commentService.addComment(bookId, request.getText(), username);
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toCommentDto(comment));
    }

    @GetMapping("/{bookId}/comments")
    public ResponseEntity<List<CommentDto>> list(@PathVariable Long bookId) {
        var comments = commentService.getComments(bookId);
        return ResponseEntity.ok(DtoMapper.toCommentDtoList(comments));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        commentService.deleteComment(id, username);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentDto> update(@PathVariable Long id, @Valid @RequestBody UpdateCommentRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        var comment = commentService.updateComment(id, request.getText(), username);
        return ResponseEntity.ok(DtoMapper.toCommentDto(comment));
    }
}

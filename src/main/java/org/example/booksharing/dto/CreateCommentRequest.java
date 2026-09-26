package org.example.booksharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateCommentRequest {
    @NotBlank(message = "Comment text is required")
    @Size(max = 5000, message = "Comment text must not exceed 5000 characters")
    private String text;

    public CreateCommentRequest() {}

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}









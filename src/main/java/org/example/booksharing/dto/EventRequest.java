package org.example.booksharing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record EventRequest(
        Long userId,
        @Positive Long bookId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String description,
        Integer priority,
        @NotNull LocalDateTime dateTime) {
}

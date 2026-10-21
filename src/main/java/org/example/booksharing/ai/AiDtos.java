package org.example.booksharing.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.example.booksharing.dto.BookDto;
import java.util.List;

public final class AiDtos {
    private AiDtos() {}
    public record Search(@NotBlank @Size(max = 1000) String query, @Size(max = 100) String category) {}
    public record Hit(BookDto book, double relevance, String explanation) {}
    public record Results(List<Hit> items, String mode, String notice) {}
    public record Preferences(@NotNull @Size(max = 12) List<@NotBlank @Size(max = 100) String> genres,
                              @NotNull @Size(max = 500) String interests, boolean useHistory) {}
    public record Chat(@NotBlank @Size(max = 1000) String message,
                       @NotNull @Size(max = 4) List<@NotBlank @Size(max = 1000) String> history,
                       boolean usePreferences) {}
    public record ChatReply(String message, List<Hit> items, String mode, String notice) {}
    public record Description(@NotBlank @Size(max = 255) String title,
                              @NotBlank @Size(max = 255) String author,
                              @NotBlank @Size(max = 5000) String description,
                              @Size(max = 100) String category,
                              @NotNull @Size(max = 15) List<@NotBlank @Size(max = 50) String> tags) {}
    public record Draft(@NotBlank @Size(max = 5000) String description,
                        @NotNull @Size(max = 15) List<@NotBlank @Size(max = 50) String> tags,
                        @NotBlank @Size(max = 100) String category, boolean draft) {}
}

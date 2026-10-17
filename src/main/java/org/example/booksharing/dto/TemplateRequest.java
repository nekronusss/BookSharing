package org.example.booksharing.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class TemplateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
    @NotBlank
    @Size(max = 20000)
    private String data;
    private Long userId;
}


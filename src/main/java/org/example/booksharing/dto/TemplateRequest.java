package org.example.booksharing.dto;
import lombok.Data;

@Data
public class TemplateRequest {
    private String name;
    private String data;
    private Long userId;
}


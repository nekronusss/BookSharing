package org.example.booksharing.dto;

import org.example.booksharing.entities.Template;

public record TemplateDto(Long id, String name, String data, Long userId) {
    public static TemplateDto from(Template template) {
        return new TemplateDto(template.getId(), template.getName(), template.getData(), template.getUser().getId());
    }
}

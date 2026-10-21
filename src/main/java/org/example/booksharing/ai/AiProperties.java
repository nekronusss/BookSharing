package org.example.booksharing.ai;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@EnableScheduling
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private boolean enabled = false;
    @Pattern(regexp = "ollama|openai-compatible")
    private String provider = "ollama";
    @NotBlank private String baseUrl = "http://localhost:11434";
    private String apiKey = "";
    @NotBlank private String chatModel = "qwen2.5:7b";
    @NotBlank private String embeddingModel = "bge-m3";
    @Min(1) @Max(120) private int timeoutSeconds = 120;
    @Min(1) @Max(1000000) private int dailyCallBudget = 1000;
    @Min(1) @Max(120) private int requestsPerMinute = 12;
    @DecimalMin("0.0") @DecimalMax("1.0") private double similarityThreshold = 0.45;
    @Min(1) @Max(100) private int indexBatchSize = 16;
    public String modelKey() {
        // The endpoint is part of the identity: different servers can expose different models with the same name.
        return BookText.hash(provider + "|" + baseUrl + "|" + embeddingModel + "|book-text-v1");
    }
}

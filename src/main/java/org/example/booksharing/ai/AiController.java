package org.example.booksharing.ai;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiSearchService search;
    private final AiAssistantService assistant;
    private final RecommendationService recommendations;
    private final AiPreferencesService preferences;
    private final UserService users;
    private final AiProperties config;
    @GetMapping("/status")
    public Map<String, Object> status() { return Map.of("enabled", config.isEnabled()); }
    @PostMapping("/search")
    public AiDtos.Results search(@Valid @RequestBody AiDtos.Search input) {
        return search.search(input.query(), input.category());
    }
    @GetMapping("/books/{id}/similar")
    public AiDtos.Results similar(@PathVariable long id) { return search.similar(id); }
    @GetMapping("/recommendations")
    public AiDtos.Results recommendations() { return recommendations.recommend(users.getCurrentUser()); }
    @GetMapping("/preferences")
    public AiDtos.Preferences preferences() { return preferences.get(users.getCurrentUser()); }
    @PutMapping("/preferences")
    public AiDtos.Preferences savePreferences(@Valid @RequestBody AiDtos.Preferences input) {
        return preferences.save(users.getCurrentUser(), input);
    }
    @PostMapping("/chat")
    public AiDtos.ChatReply chat(@Valid @RequestBody AiDtos.Chat input) {
        return assistant.chat(input, users.getCurrentUser());
    }
    @PostMapping("/books/description-suggestion")
    public AiDtos.Draft description(@Valid @RequestBody AiDtos.Description input) {
        users.getCurrentUser();
        return assistant.describe(input);
    }
}

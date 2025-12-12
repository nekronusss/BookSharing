package org.example.booksharing.contoller;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.Achievement;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.AchievementRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    @GetMapping
    public List<Achievement> getAchievements(@PathVariable Long userId) {
        return achievementRepository.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<Achievement> addAchievement(
            @PathVariable Long userId,
            @RequestBody Achievement achievement) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        achievement.setUser(user);
        Achievement saved = achievementRepository.save(achievement);

        return ResponseEntity.ok(saved);
    }
}
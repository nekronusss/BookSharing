package org.example.booksharing.contoller;

import org.example.booksharing.entities.ActionHistory;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.ActionHistoryRepository;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.PrivacySettingsDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;
    private final ActionHistoryRepository actionHistoryRepository;

    public UserController(UserRepository userRepository,
                          ActionHistoryRepository actionHistoryRepository) {
        this.userRepository = userRepository;
        this.actionHistoryRepository = actionHistoryRepository;
    }

    @GetMapping("/{id}/history")
    public List<ActionHistory> getHistory(@PathVariable Long id) {
        return actionHistoryRepository.findByUserIdOrderByCreatedAtDesc(id);
    }

    @PutMapping("/{id}/privacy")
    public void updatePrivacy(@PathVariable Long id, @RequestBody PrivacySettingsDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPrivateProfile(dto.isPrivateProfile());
        user.setPrivateBooks(dto.isPrivateBooks());
        userRepository.save(user);
    }
}

package org.example.booksharing.contoller;

import org.example.booksharing.entities.User;
import org.example.booksharing.service.UserService;
import org.example.booksharing.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class ProfileController {
    private final UserService userService;
    private final UserRepository userRepository;

    public ProfileController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PutMapping(value = "/{id}/profile", consumes = {"multipart/form-data"})
    public ResponseEntity<User> updateProfile(@PathVariable Long id,
                                              @RequestPart(value = "profile", required = false) String profileJson,
                                              @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        String authName = SecurityContextHolder.getContext().getAuthentication().getName();
        User authUser = userRepository.findByUsername(authName).orElseThrow();
        if (!authUser.getId().equals(id)) throw new RuntimeException("Forbidden");

        String displayName = null;
        String email = null;
        if (profileJson != null && !profileJson.isEmpty()) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, String> profile = mapper.readValue(profileJson, Map.class);
            displayName = profile.get("displayName");
            email = profile.get("email");
        }

        User updated = userService.updateProfile(id, displayName, email, avatar);
        return ResponseEntity.ok(updated);
    }

}

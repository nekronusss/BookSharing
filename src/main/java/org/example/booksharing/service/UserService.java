package org.example.booksharing.service;

import org.example.booksharing.entities.User;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.repository.ActionHistoryRepository;
import org.example.booksharing.entities.ActionHistory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ActionHistoryRepository historyRepository;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    public UserService(UserRepository userRepository, ActionHistoryRepository historyRepository) {
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public User updateProfile(Long userId, String displayName, String email, MultipartFile avatar) throws IOException {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (displayName != null && !displayName.isEmpty()) user.setDisplayName(displayName);
        if (email != null && !email.isEmpty()) user.setEmail(email);

        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.getSize() > 5 * 1024 * 1024) throw new RuntimeException("Avatar too large (max 5MB)");
            String contentType = avatar.getContentType();
            if (contentType == null || (!contentType.startsWith("image/")))
                throw new RuntimeException("Invalid avatar type");

            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + avatar.getOriginalFilename();
            File dest = Paths.get(UPLOAD_DIR, fileName).toFile();
            avatar.transferTo(dest);

            user.setAvatarUrl("/uploads/" + fileName);
        }

        User updated = userRepository.save(user);

        ActionHistory h = new ActionHistory();
        h.setUserId(user.getId());
        h.setActionType("UPDATE_PROFILE");
        h.setEntityType("USER");
        h.setEntityId(user.getId());
        h.setDetails("displayName=" + displayName + ", email=" + email);
        historyRepository.save(h);

        return updated;
    }
}

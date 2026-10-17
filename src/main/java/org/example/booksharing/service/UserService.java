package org.example.booksharing.service;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.ActionHistory;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.ActionHistoryRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ActionHistoryRepository historyRepository;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Authentication required"));
    }

    // Legacy clients may still send userId, but it must never select the acting user.
    public User requireCurrentUser(Long claimedUserId) {
        User user = getCurrentUser();
        if (claimedUserId != null && !user.getId().equals(claimedUserId)) {
            throw new AccessDeniedException("Cannot act as another user");
        }
        return user;
    }

    @Transactional
    public User updateProfile(String displayName, String email, MultipartFile avatar) throws IOException {
        User user = getCurrentUser();

        if (displayName != null) user.setDisplayName(displayName.trim());
        if (email != null) user.setEmail(email.isBlank() ? null : email.trim());

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = fileService.uploadFile(avatar, "avatars");
            if (user.getAvatarUrl() != null) {
                fileService.deleteFile(user.getAvatarUrl());
            }
            user.setAvatarUrl(avatarUrl);
        }

        User updated = userRepository.save(user);
        logAction(user.getId(), "UPDATE_PROFILE", "USER", user.getId(), "Profile updated");
        return updated;
    }

    @Transactional
    public void updatePrivacy(boolean privateProfile, boolean privateBooks) {
        User user = getCurrentUser();
        user.setPrivateProfile(privateProfile);
        user.setPrivateBooks(privateBooks);
        userRepository.save(user);
        logAction(user.getId(), "UPDATE_PRIVACY", "USER", user.getId(), "Privacy settings updated");
    }

    @Transactional(readOnly = true)
    public List<ActionHistory> getHistory() {
        User user = getCurrentUser();
        return historyRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    private void logAction(Long userId, String actionType, String entityType, Long entityId, String details) {
        ActionHistory h = new ActionHistory();
        h.setUserId(userId);
        h.setActionType(actionType);
        h.setEntityType(entityType);
        h.setEntityId(entityId);
        h.setDetails(details);
        historyRepository.save(h);
    }
}

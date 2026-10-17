package org.example.booksharing.controller;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.Notification;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.NotificationRepository;
import org.example.booksharing.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping({"/notifications", "/api/notifications"})
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository repo;
    private final UserService userService;

    @GetMapping
    public List<Notification> getUserNotifications(@RequestParam(required = false) Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userService.requireCurrentUser(userId).getId());
    }

    @PostMapping("/mark_read")
    @Transactional
    public void markAsRead(@RequestBody List<Long> ids) {
        Long userId = userService.getCurrentUser().getId();
        if (ids.isEmpty() || ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Provide between 1 and 100 positive notification IDs");
        }
        var uniqueIds = new HashSet<>(ids);
        List<Notification> notifications = repo.findAllById(uniqueIds);
        // Validate the entire batch before changing any entity, including managed entities.
        if (notifications.size() != uniqueIds.size()
                || notifications.stream().anyMatch(n -> !userId.equals(n.getUserId()))) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notifications.forEach(n -> n.setRead(true));
        repo.saveAll(notifications);
    }
}

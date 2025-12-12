package org.example.booksharing.contoller;

import org.example.booksharing.entities.Notification;
import org.example.booksharing.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repo;

    public NotificationController(NotificationRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Notification> getUserNotifications(@RequestParam Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId);
    }


    @PostMapping("/mark_read")
    public void markAsRead(@RequestBody List<Long> ids) {
        ids.forEach(id -> {
            repo.findById(id).ifPresent(n -> { n.setRead(true); repo.save(n); });
        });
    }
}

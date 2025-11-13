package org.example.booksharing.contoller;

import org.example.booksharing.entities.User;
import org.example.booksharing.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) { this.followService = followService; }

    @PostMapping("/{id}/follow")
    public ResponseEntity<String> follow(@PathVariable Long id, @RequestHeader("X-User-Id") Long followerId) {
        try {
            followService.follow(followerId, id);
            return ResponseEntity.ok("User " + followerId + " is now following user " + id);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @GetMapping("/{id}/followers")
    public List<User> followers(@PathVariable Long id) {
        return followService.getFollowers(id);
    }
}

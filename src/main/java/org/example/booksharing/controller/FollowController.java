package org.example.booksharing.controller;

import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.dto.UserDto;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class FollowController {
    private final FollowService followService;
    private final UserRepository userRepository;

    public FollowController(FollowService followService, UserRepository userRepository) {
        this.followService = followService;
        this.userRepository = userRepository;
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> follow(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User follower = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        followService.follow(follower.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/followers")
    public ResponseEntity<List<UserDto>> followers(@PathVariable Long id) {
        List<User> followers = followService.getFollowers(id);
        List<UserDto> followerDtos = followers.stream()
                .map(DtoMapper::toUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(followerDtos);
    }
}

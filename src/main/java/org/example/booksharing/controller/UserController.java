package org.example.booksharing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.*;
import org.example.booksharing.entities.ListItem;
import org.example.booksharing.entities.User;
import org.example.booksharing.entities.UserList;
import org.example.booksharing.service.UserListService;
import org.example.booksharing.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserListService userListService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(DtoMapper.toUserDto(user));
    }

    @PutMapping(value = "/profile", consumes = {"multipart/form-data"})
    public ResponseEntity<UserDto> updateProfile(
            @RequestPart(value = "profile", required = false) String profileJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws IOException {
        
        UpdateProfileRequest request = profileJson != null && !profileJson.isEmpty()
                ? objectMapper.readValue(profileJson, UpdateProfileRequest.class)
                : new UpdateProfileRequest();

        User updated = userService.updateProfile(request.getDisplayName(), request.getEmail(), avatar);
        return ResponseEntity.ok(DtoMapper.toUserDto(updated));
    }

    @PutMapping("/privacy")
    public ResponseEntity<Void> updatePrivacy(@RequestBody PrivacySettingsDto dto) {
        userService.updatePrivacy(dto.isPrivateProfile(), dto.isPrivateBooks());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<List<?>> getHistory() {
        return ResponseEntity.ok(userService.getHistory());
    }

    // User List endpoints
    @PostMapping("/{id}/lists")
    public ResponseEntity<UserList> createList(@PathVariable Long id, @RequestBody Map<String, String> body) {
        UserList ul = userListService.createList(id, body.get("name"));
        return ResponseEntity.ok(ul);
    }

    @PostMapping("/lists/{listId}/books")
    public ResponseEntity<ListItem> addToList(@PathVariable Long listId, @RequestBody Map<String, Long> body) {
        User user = userService.getCurrentUser();
        ListItem li = userListService.addItem(listId, body.get("bookId"), user.getId());
        return ResponseEntity.ok(li);
    }

    @GetMapping("/lists/{listId}")
    public ResponseEntity<List<ListItem>> getList(@PathVariable Long listId) {
        User user = userService.getCurrentUser();
        return ResponseEntity.ok(userListService.getItems(listId, user.getId()));
    }
}

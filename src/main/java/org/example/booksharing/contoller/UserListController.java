package org.example.booksharing.contoller;

import org.example.booksharing.entities.ListItem;
import org.example.booksharing.entities.User;
import org.example.booksharing.entities.UserList;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.service.UserListService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserListController {

    private final UserListService service;
    private final UserRepository userRepository;

    public UserListController(UserListService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @PostMapping("/{id}/lists")
    public ResponseEntity<UserList> createList(@PathVariable Long id, @RequestBody Map<String, String> body) {
        UserList ul = service.createList(id, body.get("name"));
        return ResponseEntity.ok(ul);
    }

    @PostMapping("/lists/{listId}/books")
    public ResponseEntity<ListItem> addToList(@PathVariable Long listId, @RequestBody Map<String, Long> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        Long bookId = body.get("bookId");
        ListItem li = service.addItem(listId, bookId, user.getId());
        return ResponseEntity.ok(li);
    }

    @GetMapping("/lists/{listId}")
    public ResponseEntity<List<ListItem>> getList(@PathVariable Long listId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<ListItem> items = service.getItems(listId, user.getId());
        return ResponseEntity.ok(items);
    }
}

package org.example.booksharing.contoller;

import org.example.booksharing.entities.ActionHistory;
import org.example.booksharing.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class HistoryController {
    private final HistoryService historyService;
    public HistoryController(HistoryService historyService) { this.historyService = historyService; }

    @GetMapping("/history/{id}")
    public ResponseEntity<List<ActionHistory>> history(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getUserHistory(id));
    }
}

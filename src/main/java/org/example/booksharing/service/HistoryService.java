package org.example.booksharing.service;

import org.example.booksharing.entities.ActionHistory;
import org.example.booksharing.repository.ActionHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {
    private final ActionHistoryRepository repo;
    public HistoryService(ActionHistoryRepository repo) { this.repo = repo; }
    public List<ActionHistory> getUserHistory(Long userId) { return repo.findByUserIdOrderByTimestampDesc(userId); }
}
package org.example.booksharing.repository;

import org.example.booksharing.entities.ActionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActionHistoryRepository extends JpaRepository<ActionHistory, Long> {
    List<ActionHistory> findByUserIdOrderByTimestampDesc(Long userId);
    List<ActionHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
}

package org.example.booksharing.repository;

import org.example.booksharing.entities.ListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ListItemRepository extends JpaRepository<ListItem, Long> {
    boolean existsByUserListIdAndBookId(Long userListId, Long bookId);
    List<ListItem> findByUserListId(Long userListId);
}

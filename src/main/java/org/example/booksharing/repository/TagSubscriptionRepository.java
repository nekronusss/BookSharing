package org.example.booksharing.repository;

import org.example.booksharing.entities.TagSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagSubscriptionRepository extends JpaRepository<TagSubscription, Long> {
    List<TagSubscription> findByUserId(Long userId);
}

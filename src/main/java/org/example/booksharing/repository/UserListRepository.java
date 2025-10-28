package org.example.booksharing.repository;

import org.example.booksharing.entities.UserList;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserListRepository extends JpaRepository<UserList, Long> {
    List<UserList> findByOwnerId(Long ownerId);
}

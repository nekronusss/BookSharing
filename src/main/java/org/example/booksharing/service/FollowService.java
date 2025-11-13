package org.example.booksharing.service;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Follow;
import org.example.booksharing.entities.Notification;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.FollowRepository;
import org.example.booksharing.repository.NotificationRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowService {
    private final FollowRepository repo;
    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public FollowService(FollowRepository repo, NotificationRepository notificationRepo, UserRepository userRepo) {
        this.repo = repo;
        this.notificationRepo = notificationRepo;
        this.userRepo = userRepo;
    }

    public void follow(Long followerId, Long followingId) {
        if (repo.existsByFollowerIdAndFollowingId(followerId, followingId))
            throw new RuntimeException("Already following");

        Follow f = new Follow();
        f.setFollower(userRepo.findById(followerId).orElseThrow());
        f.setFollowing(userRepo.findById(followingId).orElseThrow());
        repo.save(f);
    }

    public List<User> getFollowers(Long userId) {
        return repo.findByFollowingId(userId).stream().map(Follow::getFollower).toList();
    }

    public List<User> getFollowing(Long userId) {
        return repo.findByFollowerId(userId).stream().map(Follow::getFollowing).toList();
    }

    public void notifyFollowersAboutNewBook(User author, Book book) {
        List<Follow> followers = repo.findByFollowingId(author.getId());
        for (Follow f : followers) {
            Notification n = new Notification();
            n.setUserId(f.getFollower().getId());
            n.setType("FOLLOWING_UPDATE");
            n.setMessage(author.getUsername() + " added a new book: " + book.getTitle());
            notificationRepo.save(n);
        }
    }
}


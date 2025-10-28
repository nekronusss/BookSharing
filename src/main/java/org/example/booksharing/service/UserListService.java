package org.example.booksharing.service;

import org.example.booksharing.entities.*;
import org.example.booksharing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserListService {
    private final UserListRepository listRepo;
    private final ListItemRepository itemRepo;
    private final UserRepository userRepo;
    private final BookRepository bookRepo;
    private final ActionHistoryRepository historyRepo;


    public UserListService(UserListRepository listRepo,
                           ListItemRepository itemRepo,
                           UserRepository userRepo,
                           BookRepository bookRepo,
                           ActionHistoryRepository historyRepo) {
        this.listRepo = listRepo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.bookRepo = bookRepo;
        this.historyRepo = historyRepo;
    }

    @Transactional
    public UserList createList(Long ownerId, String name) {
        User owner = userRepo.findById(ownerId).orElseThrow();
        UserList ul = new UserList();
        ul.setName(name);
        ul.setOwner(owner);
        UserList saved = listRepo.save(ul);

        ActionHistory h = new ActionHistory();
        h.setUserId(ownerId);
        h.setActionType("CREATE_LIST");
        h.setEntityType("USER_LIST");
        h.setEntityId(saved.getId());
        h.setDetails("name=" + name);
        historyRepo.save(h);

        return saved;
    }

    @Transactional
    public ListItem addItem(Long listId, Long bookId, Long requesterId) {
        UserList ul = listRepo.findById(listId).orElseThrow();
        if (!ul.getOwner().getId().equals(requesterId)) throw new RuntimeException("Forbidden");
        if (itemRepo.existsByUserListIdAndBookId(listId, bookId)) throw new RuntimeException("Already exists");
        Book book = bookRepo.findById(bookId).orElseThrow();

        ListItem li = new ListItem();
        li.setUserList(ul);
        li.setBook(book);
        ListItem saved = itemRepo.save(li);

        ActionHistory h = new ActionHistory();
        h.setUserId(requesterId);
        h.setActionType("ADD_TO_LIST");
        h.setEntityType("LIST_ITEM");
        h.setEntityId(saved.getId());
        h.setDetails("listId=" + listId + ", bookId=" + bookId);
        historyRepo.save(h);

        return saved;
    }

    public List<ListItem> getItems(Long listId, Long requesterId) {
        UserList ul = listRepo.findById(listId).orElseThrow();
        if (!ul.getOwner().getId().equals(requesterId)) throw new RuntimeException("Forbidden");
        return itemRepo.findByUserListId(listId);
    }
}

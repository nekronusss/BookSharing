package org.example.booksharing.security;

import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.User;
import org.example.booksharing.exception.ResourceNotFoundException;

public final class ResourceAccess {
    private ResourceAccess() {}

    public static void requireOwner(User owner, User currentUser, String resource) {
        if (owner == null || !currentUser.getId().equals(owner.getId())) {
            throw new ResourceNotFoundException(resource + " not found");
        }
    }

    public static void requireVisibleBook(Book book, User currentUser) {
        User owner = book.getUser();
        if (owner == null || (!currentUser.getId().equals(owner.getId())
                && (Boolean.FALSE.equals(book.getPublic()) || owner.isPrivateBooks()))) {
            throw new ResourceNotFoundException("Book not found");
        }
    }
}

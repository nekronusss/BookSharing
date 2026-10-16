package org.example.booksharing;

import org.example.booksharing.dto.CreateBookRequest;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Exchange;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.*;
import org.example.booksharing.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoreWorkflowTest {
    private final BookRepository books = mock(BookRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final ExchangeRepository exchanges = mock(ExchangeRepository.class);
    private User user(long id, String name) { User u = new User(); u.setId(id); u.setUsername(name); return u; }
    private Exchange exchange() {
        Exchange e = new Exchange(); e.setId(10L); e.setRequester(user(1, "reader"));
        e.setOfferer(user(2, "owner")); Book book = new Book(); book.setId(20L);
        book.setTitle("Book"); book.setUser(e.getOfferer()); e.setRequestedBook(book); return e;
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void outsidersCannotReadPrivateExchange() {
        Exchange e = exchange(); when(exchanges.findById(10L)).thenReturn(Optional.of(e));
        when(users.findByUsername("outsider")).thenReturn(Optional.of(user(3, "outsider")));
        ExchangeService service = new ExchangeService(exchanges, books, users);
        assertThrows(AccessDeniedException.class, () -> service.getExchangeById(10L, "outsider"));
    }

    @Test void bothConfirmationsAreRequiredBeforeOwnershipChanges() {
        Exchange e = exchange(); e.setStatus(Exchange.ExchangeStatus.DELIVERED);
        when(exchanges.findById(10L)).thenReturn(Optional.of(e));
        when(exchanges.save(any())).thenAnswer(call -> call.getArgument(0));
        when(users.findByUsername("reader")).thenReturn(Optional.of(e.getRequester()));
        when(users.findByUsername("owner")).thenReturn(Optional.of(e.getOfferer()));
        ExchangeService service = new ExchangeService(exchanges, books, users);
        service.confirmReceipt(10L, "reader");
        assertEquals(2L, e.getRequestedBook().getUser().getId());
        assertEquals(Exchange.ExchangeStatus.DELIVERED, e.getStatus());
        service.confirmReceipt(10L, "owner");
        assertEquals(1L, e.getRequestedBook().getUser().getId());
        assertEquals(Exchange.ExchangeStatus.COMPLETED, e.getStatus());
        assertThrows(IllegalArgumentException.class, () -> service.confirmReceipt(10L, "owner"));
    }

    @Test void onlyOwnerCanEditBook() {
        Book book = new Book(); book.setId(1L); book.setUser(user(1, "owner"));
        when(books.findById(1L)).thenReturn(Optional.of(book));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("outsider", "unused"));
        BookService service = new BookService(books, users, mock(RatingRepository.class), mock(FileService.class));
        assertThrows(AccessDeniedException.class, () -> service.updateBook(1L, new CreateBookRequest(), null));
        verify(books, never()).save(any());
    }

    @Test void newBooksGetStableQrIdentifiers() {
        Book a = new Book(); Book b = new Book(); a.prePersist(); b.prePersist();
        assertNotNull(a.getQrCode()); assertNotEquals(a.getQrCode(), b.getQrCode());
        String qr = a.getQrCode(); a.preUpdate(); assertEquals(qr, a.getQrCode());
    }
}

package org.example.booksharing.ai;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Exchange.ExchangeStatus;
import org.example.booksharing.repository.BookRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import jakarta.persistence.criteria.Root;
import org.example.booksharing.entities.Exchange;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiCatalog {
    private final BookRepository books;
    public static Specification<Book> publicBooks() {
        return (root, query, cb) -> cb.and(cb.isTrue(root.get("isPublic")),
                cb.isFalse(root.get("user").get("privateBooks")));
    }
    public static Specification<Book> availableBooks() {
        return publicBooks().and((root, query, cb) -> {
            var busy = query.subquery(Long.class);
            Root<Exchange> exchange = busy.from(Exchange.class);
            busy.select(exchange.get("id")).where(cb.and(
                    cb.or(cb.equal(exchange.get("requestedBook").get("id"), root.get("id")),
                            cb.equal(exchange.get("offeredBook").get("id"), root.get("id"))),
                    exchange.get("status").in(List.of(ExchangeStatus.ACCEPTED,
                            ExchangeStatus.WAITING_FOR_SHIPMENT, ExchangeStatus.IN_TRANSIT, ExchangeStatus.DELIVERED))));
            return cb.not(cb.exists(busy));
        });
    }
    public List<Book> available() { return books.findAll(availableBooks()); }
    public List<Book> publicEntries() { return books.findAll(publicBooks()); }
    public Book publicBook(long id) {
        return books.findOne(publicBooks().and((r,q,c) -> c.equal(r.get("id"), id)))
                .orElseThrow(() -> new org.example.booksharing.exception.ResourceNotFoundException("Book not found"));
    }
}

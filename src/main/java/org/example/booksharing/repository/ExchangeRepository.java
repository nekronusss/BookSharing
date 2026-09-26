package org.example.booksharing.repository;

import org.example.booksharing.entities.Exchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExchangeRepository extends JpaRepository<Exchange, Long> {
    
    // Find all exchanges where user is requester
    @Query("SELECT e FROM Exchange e WHERE e.requester.id = :userId")
    List<Exchange> findByRequesterId(@Param("userId") Long userId);
    
    // Find all exchanges where user is offerer
    @Query("SELECT e FROM Exchange e WHERE e.offerer.id = :userId")
    List<Exchange> findByOffererId(@Param("userId") Long userId);
    
    // Find all exchanges involving a user
    @Query("SELECT e FROM Exchange e WHERE e.requester.id = :userId OR e.offerer.id = :userId")
    List<Exchange> findByUserId(@Param("userId") Long userId);
    
    // Find exchanges by status
    @Query("SELECT e FROM Exchange e WHERE e.status = :status")
    List<Exchange> findByStatus(@Param("status") Exchange.ExchangeStatus status);
    
    // Find pending exchanges for a book owner
    @Query("SELECT e FROM Exchange e WHERE e.requestedBook.id = :bookId AND e.status = 'PENDING'")
    List<Exchange> findPendingExchangesForBook(@Param("bookId") Long bookId);
    
    // Find exchanges for a specific book
    @Query("SELECT e FROM Exchange e WHERE e.requestedBook.id = :bookId OR e.offeredBook.id = :bookId")
    List<Exchange> findByBookId(@Param("bookId") Long bookId);
}


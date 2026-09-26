package org.example.booksharing.service;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.CreateExchangeRequest;
import org.example.booksharing.dto.ExchangeDto;
import org.example.booksharing.entities.Book;
import org.example.booksharing.entities.Exchange;
import org.example.booksharing.entities.User;
import org.example.booksharing.exception.ResourceNotFoundException;
import org.example.booksharing.repository.BookRepository;
import org.example.booksharing.repository.ExchangeRepository;
import org.example.booksharing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExchangeService {
    
    private final ExchangeRepository exchangeRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public ExchangeDto createExchange(CreateExchangeRequest request, String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Book requestedBook = bookRepository.findById(request.getRequestedBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested book not found"));
        
        // Check if requester owns the requested book
        if (requestedBook.getUser().getId().equals(requester.getId())) {
            throw new IllegalArgumentException("You cannot request your own book");
        }
        
        // Check if there's already a pending exchange for this book
        List<Exchange> existingExchanges = exchangeRepository.findPendingExchangesForBook(request.getRequestedBookId());
        boolean hasPendingRequest = existingExchanges.stream()
                .anyMatch(e -> e.getRequester().getId().equals(requester.getId()));
        if (hasPendingRequest) {
            throw new IllegalArgumentException("You already have a pending exchange request for this book");
        }
        
        Exchange exchange = new Exchange();
        exchange.setRequester(requester);
        exchange.setOfferer(requestedBook.getUser());
        exchange.setRequestedBook(requestedBook);
        exchange.setStatus(Exchange.ExchangeStatus.PENDING);
        exchange.setMessage(request.getMessage());
        
        // Handle offered book if provided
        if (request.getOfferedBookId() != null) {
            Book offeredBook = bookRepository.findById(request.getOfferedBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Offered book not found"));
            
            // Verify requester owns the offered book
            if (!offeredBook.getUser().getId().equals(requester.getId())) {
                throw new IllegalArgumentException("You do not own the offered book");
            }
            exchange.setOfferedBook(offeredBook);
        }
        
        // QR code will be generated when exchange is accepted
        Exchange savedExchange = exchangeRepository.save(exchange);
        return toExchangeDto(savedExchange, requester.getId());
    }
    
    @Transactional
    public ExchangeDto acceptExchange(Long exchangeId, String offererUsername) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User offerer = userRepository.findByUsername(offererUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the user is the offerer
        if (!exchange.getOfferer().getId().equals(offerer.getId())) {
            throw new IllegalArgumentException("Only the book owner can accept this exchange");
        }
        
        // Verify status is PENDING
        if (exchange.getStatus() != Exchange.ExchangeStatus.PENDING) {
            throw new IllegalArgumentException("Only pending exchanges can be accepted");
        }
        
        exchange.setStatus(Exchange.ExchangeStatus.WAITING_FOR_SHIPMENT);
        Exchange savedExchange = exchangeRepository.save(exchange);
        
        return toExchangeDto(savedExchange, offerer.getId());
    }
    
    @Transactional
    public ExchangeDto rejectExchange(Long exchangeId, String offererUsername) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User offerer = userRepository.findByUsername(offererUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the user is the offerer
        if (!exchange.getOfferer().getId().equals(offerer.getId())) {
            throw new IllegalArgumentException("Only the book owner can reject this exchange");
        }
        
        // Verify status is PENDING
        if (exchange.getStatus() != Exchange.ExchangeStatus.PENDING) {
            throw new IllegalArgumentException("Only pending exchanges can be rejected");
        }
        
        exchange.setStatus(Exchange.ExchangeStatus.REJECTED);
        Exchange savedExchange = exchangeRepository.save(exchange);
        
        return toExchangeDto(savedExchange, offerer.getId());
    }
    
    @Transactional
    public ExchangeDto markAsShipped(Long exchangeId, String trackingNumber, String username) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the user is the offerer
        if (!exchange.getOfferer().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the offerer can mark the exchange as shipped");
        }
        
        // Verify status is WAITING_FOR_SHIPMENT
        if (exchange.getStatus() != Exchange.ExchangeStatus.WAITING_FOR_SHIPMENT) {
            throw new IllegalArgumentException("Exchange must be in WAITING_FOR_SHIPMENT status");
        }
        
        exchange.setTrackingNumber(trackingNumber);
        exchange.setStatus(Exchange.ExchangeStatus.IN_TRANSIT);
        Exchange savedExchange = exchangeRepository.save(exchange);
        
        return toExchangeDto(savedExchange, user.getId());
    }
    
    @Transactional
    public ExchangeDto markAsDelivered(Long exchangeId, String username) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the user is the requester (the one receiving the requested book)
        if (!exchange.getRequester().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the requester can mark the exchange as delivered");
        }
        
        // Verify status is IN_TRANSIT
        if (exchange.getStatus() != Exchange.ExchangeStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Exchange must be in IN_TRANSIT status");
        }
        
        exchange.setStatus(Exchange.ExchangeStatus.DELIVERED);
        Exchange savedExchange = exchangeRepository.save(exchange);
        
        return toExchangeDto(savedExchange, user.getId());
    }
    
    @Transactional
    public ExchangeDto confirmReceipt(Long exchangeId, String username) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the user is part of the exchange
        boolean isRequester = exchange.getRequester().getId().equals(user.getId());
        boolean isOfferer = exchange.getOfferer().getId().equals(user.getId());
        
        if (!isRequester && !isOfferer) {
            throw new IllegalArgumentException("You are not part of this exchange");
        }
        
        // Verify status is DELIVERED
        if (exchange.getStatus() != Exchange.ExchangeStatus.DELIVERED) {
            throw new IllegalArgumentException("Exchange must be in DELIVERED status before confirming receipt");
        }
        
        // Mark confirmation
        if (isRequester) {
            exchange.setRequesterConfirmedReceipt(true);
        } else {
            exchange.setOffererConfirmedReceipt(true);
        }
        
        // If both parties confirmed, complete the exchange
        if (exchange.getRequesterConfirmedReceipt() && exchange.getOffererConfirmedReceipt()) {
            exchange.setStatus(Exchange.ExchangeStatus.COMPLETED);
            transferBooks(exchange);
        }
        
        Exchange savedExchange = exchangeRepository.save(exchange);
        return toExchangeDto(savedExchange, user.getId());
    }
    
    @Transactional
    public ExchangeDto cancelExchange(Long exchangeId, String username) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify the user is the requester
        if (!exchange.getRequester().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Only the requester can cancel this exchange");
        }
        
        // Verify status is PENDING
        if (exchange.getStatus() != Exchange.ExchangeStatus.PENDING) {
            throw new IllegalArgumentException("Only pending exchanges can be cancelled");
        }
        
        exchange.setStatus(Exchange.ExchangeStatus.CANCELLED);
        Exchange savedExchange = exchangeRepository.save(exchange);
        
        return toExchangeDto(savedExchange, user.getId());
    }
    
    @Transactional
    protected void transferBooks(Exchange exchange) {
        // Transfer requested book from offerer to requester
        Book requestedBook = exchange.getRequestedBook();
        requestedBook.setUser(exchange.getRequester());
        
        // Transfer offered book from requester to offerer (if exists)
        if (exchange.getOfferedBook() != null) {
            Book offeredBook = exchange.getOfferedBook();
            offeredBook.setUser(exchange.getOfferer());
            bookRepository.save(offeredBook);
        }
        
        bookRepository.save(requestedBook);
    }
    
    @Transactional(readOnly = true)
    public ExchangeDto getExchangeById(Long exchangeId, String username) {
        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return toExchangeDto(exchange, user.getId());
    }
    
    @Transactional(readOnly = true)
    public List<ExchangeDto> getUserExchanges(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Exchange> exchanges = exchangeRepository.findByUserId(user.getId());
        return exchanges.stream()
                .map(e -> toExchangeDto(e, user.getId()))
                .collect(Collectors.toList());
    }
    
    private ExchangeDto toExchangeDto(Exchange exchange, Long currentUserId) {
        ExchangeDto dto = new ExchangeDto();
        dto.setId(exchange.getId());
        dto.setRequesterId(exchange.getRequester().getId());
        dto.setRequesterUsername(exchange.getRequester().getUsername());
        dto.setOffererId(exchange.getOfferer().getId());
        dto.setOffererUsername(exchange.getOfferer().getUsername());
        dto.setRequestedBookId(exchange.getRequestedBook().getId());
        dto.setRequestedBookTitle(exchange.getRequestedBook().getTitle());
        
        if (exchange.getOfferedBook() != null) {
            dto.setOfferedBookId(exchange.getOfferedBook().getId());
            dto.setOfferedBookTitle(exchange.getOfferedBook().getTitle());
        }
        
        dto.setStatus(exchange.getStatus());
        dto.setMessage(exchange.getMessage());
        dto.setTrackingNumber(exchange.getTrackingNumber());
        dto.setRequesterConfirmedReceipt(exchange.getRequesterConfirmedReceipt());
        dto.setOffererConfirmedReceipt(exchange.getOffererConfirmedReceipt());
        dto.setCreatedAt(exchange.getCreatedAt());
        dto.setUpdatedAt(exchange.getUpdatedAt());
        dto.setAcceptedAt(exchange.getAcceptedAt());
        dto.setCompletedAt(exchange.getCompletedAt());
        
        // Determine available actions based on status and user role
        boolean isRequester = exchange.getRequester().getId().equals(currentUserId);
        boolean isOfferer = exchange.getOfferer().getId().equals(currentUserId);
        
        switch (exchange.getStatus()) {
            case PENDING:
                dto.setCanAccept(isOfferer);
                dto.setCanReject(isOfferer);
                dto.setCanCancel(isRequester);
                break;
            case WAITING_FOR_SHIPMENT:
                dto.setCanMarkAsShipped(isOfferer);
                break;
            case IN_TRANSIT:
                dto.setCanMarkAsDelivered(isRequester);
                break;
            case DELIVERED:
                dto.setCanConfirmReceipt(true);  // Both can confirm
                break;
            case COMPLETED:
            case REJECTED:
            case CANCELLED:
                // No actions available for completed/rejected/cancelled exchanges
                break;
            case ACCEPTED:
                // Legacy status, should not occur in new flow
                break;
        }
        
        return dto;
    }
}


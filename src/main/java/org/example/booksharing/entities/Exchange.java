package org.example.booksharing.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
public class Exchange {
    
    public enum ExchangeStatus {
        PENDING,                    // Exchange request created, waiting for acceptance
        ACCEPTED,                   // Offerer accepted the request
        WAITING_FOR_SHIPMENT,       // Waiting for books to be shipped
        IN_TRANSIT,                 // Books are in transit
        DELIVERED,                  // Books have been delivered
        COMPLETED,                  // Both parties confirmed receipt, exchange completed
        REJECTED,                   // Offerer rejected the request
        CANCELLED                   // Request cancelled by requester
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offerer_id", nullable = false)
    private User offerer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_book_id", nullable = false)
    private Book requestedBook;  // Book that requester wants
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_book_id")
    private Book offeredBook;    // Book that requester offers in return (optional)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status = ExchangeStatus.PENDING;
    
    private String message;  // Optional message from requester
    
    private String trackingNumber;  // Optional tracking number for shipment
    
    private Boolean requesterConfirmedReceipt = false;  // Requester confirmed receipt
    private Boolean offererConfirmedReceipt = false;    // Offerer confirmed receipt
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime acceptedAt;
    
    private LocalDateTime completedAt;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getRequester() {
        return requester;
    }
    
    public void setRequester(User requester) {
        this.requester = requester;
    }
    
    public User getOfferer() {
        return offerer;
    }
    
    public void setOfferer(User offerer) {
        this.offerer = offerer;
    }
    
    public Book getRequestedBook() {
        return requestedBook;
    }
    
    public void setRequestedBook(Book requestedBook) {
        this.requestedBook = requestedBook;
    }
    
    public Book getOfferedBook() {
        return offeredBook;
    }
    
    public void setOfferedBook(Book offeredBook) {
        this.offeredBook = offeredBook;
    }
    
    public ExchangeStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExchangeStatus status) {
        this.status = status;
        if ((status == ExchangeStatus.ACCEPTED || status == ExchangeStatus.WAITING_FOR_SHIPMENT) && acceptedAt == null) {
            acceptedAt = LocalDateTime.now();
        }
        if (status == ExchangeStatus.COMPLETED && completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTrackingNumber() {
        return trackingNumber;
    }
    
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    
    public Boolean getRequesterConfirmedReceipt() {
        return requesterConfirmedReceipt != null && requesterConfirmedReceipt;
    }
    
    public void setRequesterConfirmedReceipt(Boolean requesterConfirmedReceipt) {
        this.requesterConfirmedReceipt = requesterConfirmedReceipt;
    }
    
    public Boolean getOffererConfirmedReceipt() {
        return offererConfirmedReceipt != null && offererConfirmedReceipt;
    }
    
    public void setOffererConfirmedReceipt(Boolean offererConfirmedReceipt) {
        this.offererConfirmedReceipt = offererConfirmedReceipt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}


package org.example.booksharing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.example.booksharing.entities.Exchange;

import java.time.LocalDateTime;

public class ExchangeDto {
    private Long id;
    
    @JsonProperty("requesterId")
    private Long requesterId;
    
    @JsonProperty("requesterUsername")
    private String requesterUsername;
    
    @JsonProperty("offererId")
    private Long offererId;
    
    @JsonProperty("offererUsername")
    private String offererUsername;
    
    @JsonProperty("requestedBookId")
    private Long requestedBookId;
    
    @JsonProperty("requestedBookTitle")
    private String requestedBookTitle;
    
    @JsonProperty("offeredBookId")
    private Long offeredBookId;
    
    @JsonProperty("offeredBookTitle")
    private String offeredBookTitle;
    
    private Exchange.ExchangeStatus status;
    
    private String message;
    
    private String trackingNumber;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("acceptedAt")
    private LocalDateTime acceptedAt;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("completedAt")
    private LocalDateTime completedAt;
    
    @JsonProperty("canMarkAsShipped")
    private Boolean canMarkAsShipped = false;
    
    @JsonProperty("canMarkAsDelivered")
    private Boolean canMarkAsDelivered = false;
    
    @JsonProperty("canConfirmReceipt")
    private Boolean canConfirmReceipt = false;
    
    @JsonProperty("requesterConfirmedReceipt")
    private Boolean requesterConfirmedReceipt = false;
    
    @JsonProperty("offererConfirmedReceipt")
    private Boolean offererConfirmedReceipt = false;
    
    @JsonProperty("canAccept")
    private Boolean canAccept = false;
    
    @JsonProperty("canReject")
    private Boolean canReject = false;
    
    @JsonProperty("canCancel")
    private Boolean canCancel = false;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRequesterId() {
        return requesterId;
    }
    
    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }
    
    public String getRequesterUsername() {
        return requesterUsername;
    }
    
    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }
    
    public Long getOffererId() {
        return offererId;
    }
    
    public void setOffererId(Long offererId) {
        this.offererId = offererId;
    }
    
    public String getOffererUsername() {
        return offererUsername;
    }
    
    public void setOffererUsername(String offererUsername) {
        this.offererUsername = offererUsername;
    }
    
    public Long getRequestedBookId() {
        return requestedBookId;
    }
    
    public void setRequestedBookId(Long requestedBookId) {
        this.requestedBookId = requestedBookId;
    }
    
    public String getRequestedBookTitle() {
        return requestedBookTitle;
    }
    
    public void setRequestedBookTitle(String requestedBookTitle) {
        this.requestedBookTitle = requestedBookTitle;
    }
    
    public Long getOfferedBookId() {
        return offeredBookId;
    }
    
    public void setOfferedBookId(Long offeredBookId) {
        this.offeredBookId = offeredBookId;
    }
    
    public String getOfferedBookTitle() {
        return offeredBookTitle;
    }
    
    public void setOfferedBookTitle(String offeredBookTitle) {
        this.offeredBookTitle = offeredBookTitle;
    }
    
    public Exchange.ExchangeStatus getStatus() {
        return status;
    }
    
    public void setStatus(Exchange.ExchangeStatus status) {
        this.status = status;
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
    
    public Boolean getCanMarkAsShipped() {
        return canMarkAsShipped;
    }
    
    public void setCanMarkAsShipped(Boolean canMarkAsShipped) {
        this.canMarkAsShipped = canMarkAsShipped;
    }
    
    public Boolean getCanMarkAsDelivered() {
        return canMarkAsDelivered;
    }
    
    public void setCanMarkAsDelivered(Boolean canMarkAsDelivered) {
        this.canMarkAsDelivered = canMarkAsDelivered;
    }
    
    public Boolean getCanConfirmReceipt() {
        return canConfirmReceipt;
    }
    
    public void setCanConfirmReceipt(Boolean canConfirmReceipt) {
        this.canConfirmReceipt = canConfirmReceipt;
    }
    
    public Boolean getCanAccept() {
        return canAccept;
    }
    
    public void setCanAccept(Boolean canAccept) {
        this.canAccept = canAccept;
    }
    
    public Boolean getCanReject() {
        return canReject;
    }
    
    public void setCanReject(Boolean canReject) {
        this.canReject = canReject;
    }
    
    public Boolean getCanCancel() {
        return canCancel;
    }
    
    public void setCanCancel(Boolean canCancel) {
        this.canCancel = canCancel;
    }
    
    public Boolean getRequesterConfirmedReceipt() {
        return requesterConfirmedReceipt;
    }
    
    public void setRequesterConfirmedReceipt(Boolean requesterConfirmedReceipt) {
        this.requesterConfirmedReceipt = requesterConfirmedReceipt;
    }
    
    public Boolean getOffererConfirmedReceipt() {
        return offererConfirmedReceipt;
    }
    
    public void setOffererConfirmedReceipt(Boolean offererConfirmedReceipt) {
        this.offererConfirmedReceipt = offererConfirmedReceipt;
    }
}


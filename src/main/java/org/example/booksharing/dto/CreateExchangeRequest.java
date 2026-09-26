package org.example.booksharing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateExchangeRequest {
    @JsonProperty("requestedBookId")
    private Long requestedBookId;
    
    @JsonProperty("offeredBookId")
    private Long offeredBookId;  // Optional
    
    @JsonProperty("message")
    private String message;  // Optional
    
    public Long getRequestedBookId() {
        return requestedBookId;
    }
    
    public void setRequestedBookId(Long requestedBookId) {
        this.requestedBookId = requestedBookId;
    }
    
    public Long getOfferedBookId() {
        return offeredBookId;
    }
    
    public void setOfferedBookId(Long offeredBookId) {
        this.offeredBookId = offeredBookId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}




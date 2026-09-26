package org.example.booksharing.dto;

import jakarta.validation.constraints.Email;

public class UpdateProfileRequest {
    private String displayName;
    
    @Email(message = "Invalid email format")
    private String email;

    public UpdateProfileRequest() {}

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}









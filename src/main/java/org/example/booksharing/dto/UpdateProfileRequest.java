package org.example.booksharing.dto;

import jakarta.validation.constraints.Email;

public class UpdateProfileRequest {
    @jakarta.validation.constraints.Size(max = 100)
    private String displayName;
    
    @Email(message = "Invalid email format")
    @jakarta.validation.constraints.Size(max = 254)
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









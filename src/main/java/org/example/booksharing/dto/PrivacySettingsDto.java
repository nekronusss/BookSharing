package org.example.booksharing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrivacySettingsDto {
    @JsonProperty("privateProfile")
    private boolean privateProfile = false;
    
    @JsonProperty("privateBooks")
    private boolean privateBooks = false;

    public boolean isPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    public boolean isPrivateBooks() {
        return privateBooks;
    }

    public void setPrivateBooks(boolean privateBooks) {
        this.privateBooks = privateBooks;
    }
}









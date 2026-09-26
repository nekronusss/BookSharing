package org.example.booksharing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserDto {
    private Long id;
    private String username;
    
    @JsonProperty("displayName")
    private String displayName;
    
    private String email;
    
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    
    @JsonProperty("privateProfile")
    private Boolean privateProfile = false;
    
    @JsonProperty("privateBooks")
    private Boolean privateBooks = false;

    public UserDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

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

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Boolean getPrivateProfile() {
        return privateProfile;
    }

    public void setPrivateProfile(Boolean privateProfile) {
        this.privateProfile = privateProfile;
    }

    public Boolean getPrivateBooks() {
        return privateBooks;
    }

    public void setPrivateBooks(Boolean privateBooks) {
        this.privateBooks = privateBooks;
    }
}


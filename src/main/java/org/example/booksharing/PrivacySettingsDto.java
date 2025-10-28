package org.example.booksharing;

public class PrivacySettingsDto {
    private boolean privateProfile;
    private boolean privateBooks;

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

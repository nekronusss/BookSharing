package org.example.booksharing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RatingDto {
    private Long id;
    
    private Integer score;
    
    private Boolean liked = false;
    
    @JsonProperty("bookId")
    private Long bookId;
    
    @JsonProperty("userId")
    private Long userId;
    
    @JsonProperty("username")
    private String username;

    public RatingDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}









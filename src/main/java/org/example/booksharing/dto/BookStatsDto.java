package org.example.booksharing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BookStatsDto {
    @JsonProperty("likes")
    private Long likes = 0L;
    
    @JsonProperty("avgRating")
    private Double avgRating = 0.0;

    public BookStatsDto() {}

    public BookStatsDto(Long likes, Double avgRating) {
        this.likes = likes != null ? likes : 0L;
        this.avgRating = avgRating != null ? avgRating : 0.0;
    }

    public Long getLikes() {
        return likes;
    }

    public void setLikes(Long likes) {
        this.likes = likes != null ? likes : 0L;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating != null ? avgRating : 0.0;
    }
}









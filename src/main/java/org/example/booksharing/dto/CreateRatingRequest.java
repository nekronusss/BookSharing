package org.example.booksharing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class CreateRatingRequest {
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score must be at most 5")
    private Integer score;
    
    private Boolean liked = false;

    public CreateRatingRequest() {}

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
}









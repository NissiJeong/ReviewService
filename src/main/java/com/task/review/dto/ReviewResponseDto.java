package com.task.review.dto;

import com.task.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long userId;
    private int score;
    private String content;
    private String imageUrl;

    public ReviewResponseDto(Review review) {
        this.userId = review.getUserId();
        this.score = review.getScore();
        this.content = review.getContent();
        this.imageUrl = review.getImageUrl();
    }
}

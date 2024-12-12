package com.task.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private Long userId;

    @Min(value = 1, message = "점수는 1~5점 사이의 점수를 부여할 수 있습니다.")
    @Max(value = 5, message = "점수는 1~5점 사이의 점수를 부여할 수 있습니다.")
    private int score;

    private String content;
    private String imageUrl;
}

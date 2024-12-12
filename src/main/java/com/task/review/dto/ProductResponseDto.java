package com.task.review.dto;

import com.task.review.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ProductResponseDto {
    private Long totalCount;
    private double score;
    private Long cursor;
    private List<ReviewResponseDto> reviews;

    public ProductResponseDto(Product product) {
        this.totalCount = product.getReviewCount();
        this.score = product.getScore();
    }
}

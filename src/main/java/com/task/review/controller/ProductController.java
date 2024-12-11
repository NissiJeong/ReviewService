package com.task.review.controller;

import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody ReviewRequestDto requestDto) {

        ReviewResponseDto reviewResponseDto = productService.createReview(productId, requestDto);

        return ResponseEntity.ok(reviewResponseDto);
    }
}

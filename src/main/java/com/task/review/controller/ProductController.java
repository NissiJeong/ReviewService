package com.task.review.controller;

import com.task.review.dto.CursorResult;
import com.task.review.dto.ProductResponseDto;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private static final int DEFAULT_SIZE = 10;

    private final ProductService productService;

    @PostMapping("/{productId}/reviews")
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestBody ReviewRequestDto requestDto) {

        ReviewResponseDto reviewResponseDto = productService.createReview(productId, requestDto);

        return ResponseEntity.ok(reviewResponseDto);
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long productId, @RequestParam("cursor") Long cursor, @RequestParam("size") Integer size) {
        if(size == null) size = DEFAULT_SIZE;
        ProductResponseDto result = productService.getReviews(productId, cursor, size);
        return ResponseEntity.ok(result);
    }
}

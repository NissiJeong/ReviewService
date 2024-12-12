package com.task.review.controller;

import com.task.review.dto.ProductResponseDto;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ReviewController {

    private static final int DEFAULT_SIZE = 10;

    private final ReviewService productService;

    @PostMapping(value = "/{productId}/reviews", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createReview(@PathVariable Long productId, @RequestPart ReviewRequestDto requestDto,
    @RequestPart(value="image", required = false) MultipartFile file) {
        ReviewResponseDto reviewResponseDto = productService.createReview(productId, requestDto, file);

        return ResponseEntity.ok(reviewResponseDto);
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<?> getReviews(@PathVariable Long productId, @RequestParam("cursor") Long cursor, @RequestParam("size") Integer size) {
        if(size == null) size = DEFAULT_SIZE;
        ProductResponseDto result = productService.getReviews(productId, cursor, size);
        return ResponseEntity.ok(result);
    }
}

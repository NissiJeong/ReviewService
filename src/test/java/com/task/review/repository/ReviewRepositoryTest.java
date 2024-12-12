package com.task.review.repository;

import com.task.review.dto.ReviewRequestDto;
import com.task.review.entity.Product;
import com.task.review.entity.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ReviewRepositoryTest {

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    ProductRepository productRepository;

    Product product;

    @BeforeEach
    void before() {
        product = productRepository.findById(1L).orElseThrow();
    }

    @Test
    @DisplayName("리뷰 만들기 테스트")
    void createReview() {
        /*
        given
         */
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(1L, 7, "content", null);
        Review review = new Review(reviewRequestDto, product);

        /*
        when
         */
        Review result = reviewRepository.save(review);

        /*
        then
         */
        assertEquals(result.getId(), review.getId());
    }

    @Test
    @DisplayName("리뷰 조회 테스트")
    void reviewList() {
        /*
        given
         */
        ReviewRequestDto reviewRequestDto1 = new ReviewRequestDto(1L, 7, "content", null);
        ReviewRequestDto reviewRequestDto2 = new ReviewRequestDto(2L, 7, "content", null);
        Review review1 = new Review(reviewRequestDto1, product);
        Review review2 = new Review(reviewRequestDto2, product);
        reviewRepository.save(review1);
        reviewRepository.save(review2);

        /*
        when
         */
        List<Review> result = reviewRepository.findAll();

        /*
        then
         */
        assertEquals(result.size(), 2);
    }
}

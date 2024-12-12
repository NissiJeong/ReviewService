package com.task.review.service;

import com.task.review.dto.ReviewRequestDto;
import com.task.review.entity.Product;
import com.task.review.repository.ProductRepository;
import com.task.review.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ReviewRepository reviewRepository;

    @Autowired
    S3ImageUploadDummyService imageService;

    Product product;

    @BeforeEach
    public void before() {
        Product saveProduct = new Product();
        saveProduct.setReviewCount(0L);
        saveProduct.setScore(0);
        product = productRepository.saveAndFlush(saveProduct);
    }

    @AfterEach
    public void after() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("리뷰 등록 테스트")
    @Transactional
    void createReview() {
        reviewService.createReview(product.getId(), new ReviewRequestDto(7L, 5, "content", null), null);

        Product result = productRepository.findById(product.getId()).orElseThrow();

        assertEquals(1, result.getReviewCount());
    }

    @Test
    @DisplayName("동시에 100개 리뷰 작성 요청")
    public void threadCreateReview() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for(int i=0; i<threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
               try {
                   System.out.println("finalI= "+finalI);
                   reviewService.createReview(product.getId(), new ReviewRequestDto((long) finalI, 5, "content", null), null);
               } catch (Exception e) {
                   System.err.println("Thread " + finalI + " failed: " + e.getMessage());
               } finally {
                   latch.countDown();
               }
            });
        }

        latch.await();

        Product result = productRepository.findById(product.getId()).orElseThrow();

        assertEquals(100, result.getReviewCount());
    }
}

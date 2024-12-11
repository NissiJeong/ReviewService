package com.task.review.service;

import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.entity.Product;
import com.task.review.entity.Review;
import com.task.review.repository.ProductRepository;
import com.task.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ReviewResponseDto createReview(Long productId, ReviewRequestDto requestDto) {
        // 상품이 없으면 리뷰 저장 못 함
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );

        // 리뷰 저장
        Review review = reviewRepository.save(new Review(requestDto, product));

        // 리뷰 저장 후 프로덕트 리뷰수, 스코어 업데이트
        // 리뷰수는  +1 하면 되고 스코어 = ((스코어*리뷰수) + 입력된 스코어) / 리뷰수 + 1);
        double newScore = (product.getScore()*product.getReviewCount()+ requestDto.getScore())/(product.getReviewCount()+1);
        product.update(product.getReviewCount()+1, newScore);

        return new ReviewResponseDto(review);
    }
}

package com.task.review.service;

import com.task.review.dto.ProductResponseDto;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.entity.Product;
import com.task.review.entity.Review;
import com.task.review.repository.ProductRepository;
import com.task.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // 해당 유저가 이미 상품 리뷰를 작성했으면 못하게 처리
        List<Review> reviews = reviewRepository.findAllByProductAndUserId(product, requestDto.getUserId());
        if(!reviews.isEmpty()) {
            throw new IllegalArgumentException("이미 해당 상품에 리뷰를 작성하였습니다.");
        }

        // 리뷰 저장
        Review review = reviewRepository.save(new Review(requestDto, product));

        // 리뷰 저장 후 프로덕트 리뷰수, 스코어 업데이트
        // 리뷰수는  +1 하면 되고 스코어 = ((스코어*리뷰수) + 입력된 스코어) / 리뷰수 + 1);
        double newScore = (product.getScore()*product.getReviewCount()+ requestDto.getScore())/(product.getReviewCount()+1);
        product.update(product.getReviewCount()+1, newScore);

        return new ReviewResponseDto(review);
    }

    public ProductResponseDto getReviews(Long productId, Long cursor, int pageSize) {
        // cursor가 0 인경우 최근 데이터부터 가져오도록
        if(cursor == null || cursor == 0) cursor = Long.MAX_VALUE;

        Pageable pageable = PageRequest.of(0, pageSize); //한 번에 가져올 크기

        // 상품이 없으면 조회 되지 않도록
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new NullPointerException("해당 상품을 찾을 수 없습니다.")
        );
        ProductResponseDto responseDto = new ProductResponseDto(product);

        // 커서 값이 1인 경우 리턴될 리뷰가 없음.
        if(cursor == 1) {
            return responseDto;
        }
        List<Review> reviews = reviewRepository.findByProductIdAndIdLessThanOrderByIdDesc(productId, cursor, pageable);

        List<ReviewResponseDto> reviewResponseList = reviews.stream().map(ReviewResponseDto::new).toList();
        responseDto.setReviews(reviewResponseList);
        responseDto.setCursor(reviews.get(reviews.size()-1).getId());
        return responseDto;
    }
}

package com.task.review.service;

import com.task.review.dto.ProductResponseDto;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.entity.Product;
import com.task.review.entity.Review;
import com.task.review.repository.ProductRepository;
import com.task.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final S3ImageUploadDummyService imageService;

    private final RedissonClient redissonClient;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReviewResponseDto createReview(Long productId, ReviewRequestDto requestDto, MultipartFile file) {
        String lockKey = "lock:product:review:"+productId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean available = false;

        try {
            available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            if(!available) {
                throw new IllegalArgumentException("Lock 획득 실패");
            }

            // 상품이 없으면 리뷰 저장 못 함
            Product product = productRepository.findById(productId).orElseThrow(() ->
                    new NullPointerException("해당 상품을 찾을 수 없습니다.")
            );

            // 해당 유저가 이미 상품 리뷰를 작성했으면 못하게 처리
            List<Review> reviews = reviewRepository.findAllByProductAndUserId(product, requestDto.getUserId());
            if(!reviews.isEmpty()) {
                throw new IllegalArgumentException("이미 해당 상품에 리뷰를 작성하였습니다.");
            }

            // 이미지 파일 S3에 저장(dummy)
            if(file != null) {
                String imageUrl =imageService.upploadImageToS3(file);
                requestDto.setImageUrl(imageUrl);
            }

            // 리뷰 저장
            Review review = reviewRepository.save(new Review(requestDto, product));

            // 리뷰 저장 후 프로덕트 리뷰수, 스코어 업데이트
            // 리뷰수는  +1 하면 되고 스코어 = ((스코어*리뷰수) + 입력된 스코어) / 리뷰수 + 1);
            double newScore = (product.getScore()*product.getReviewCount()+ requestDto.getScore())/(product.getReviewCount()+1);
            product.update(product.getReviewCount()+1, newScore);

            // 리뷰카운트, 스코어 업데이트 될때마다 바로 db 반영.
            // 트랜잭션이 커밋되기 전에 다른 스레드가 잡아버리면 문제가 발생할 수 있음.
            productRepository.saveAndFlush(product);

            return new ReviewResponseDto(review);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(available)
                lock.unlock();
        }
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

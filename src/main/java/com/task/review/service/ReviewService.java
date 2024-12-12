package com.task.review.service;

import com.task.review.dto.ProductResponseDto;
import com.task.review.dto.ReviewRequestDto;
import com.task.review.dto.ReviewResponseDto;
import com.task.review.entity.Product;
import com.task.review.entity.Review;
import com.task.review.repository.ProductRepository;
import com.task.review.repository.RedisRepository;
import com.task.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final S3ImageUploadDummyService imageService;
    private final RedisRepository redisRepository;

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

            // Redis 에 리뷰 데이터 업데이트
            redisRepository.incrementReviewData(productId, requestDto.getScore());

            // 리뷰 저장
            Review review = reviewRepository.save(new Review(requestDto, product));

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

        // Redis 에서 프로덕트 id 값으로 리뷰 카운트, 리뷰 스코어 가져오기
        Map<String, Long> reviewInfo = redisRepository.getReviewData(product.getId());
        // Redis 에서 가져온 값이 있으면 product update
        if(reviewInfo.get("reviewCount") != null && reviewInfo.get("scoreSum") != null) {
            long reviewCount = reviewInfo.get("reviewCount");
            long scoreSum = reviewInfo.get("scoreSum");
            double score = scoreSum / (double) reviewCount;
            product.setReviewCount(reviewCount);
            product.setScore(score);
        }

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

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void syncRedisToMySQL() {
        String lockKey = "lock:sync:product";
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 락 획득 시도
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                // Redis에서 동기화할 키 조회
                Set<String> keys = redisRepository.getProductReviewKey();
                if (keys == null || keys.isEmpty()) return;

                for (String key : keys) {
                    Long productId = Long.valueOf(key.split(":")[1]);
                    syncProductData(productId);
                }
            } else {
                System.out.println("동기화 작업 중복 실행 방지됨");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("동기화 중 락 획득 실패", e);
        } finally {
            // 락 해제
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    private void syncProductData(Long productId) {
        Map<String, Long> reviewInfo = redisRepository.getReviewData(productId);
        long reviewCount = reviewInfo.get("reviewCount") == null? 0L : reviewInfo.get("reviewCount");
        long scoreSum = reviewInfo.get("scoreSum") == null? 0L : reviewInfo.get("scoreSum");

        Product product = productRepository.findById(productId).orElseThrow();
        double newScore = reviewCount > 0 ? scoreSum / (double) reviewCount : 0.0;

        product.update(reviewCount, newScore);
        productRepository.save(product);

        // Redis 데이터 초기화
        redisRepository.deleteReviewInfo(productId);
    }
}

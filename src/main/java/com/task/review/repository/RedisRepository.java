package com.task.review.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class RedisRepository {
    private RedisTemplate<String, String> redisTemplate;

    public void incrementReviewData(Long productId, int score) {
        String reviewCountKey = "product:"+productId+":reviewCount";
        String scoreSumKey = "product:"+productId+":scoreSum";

        redisTemplate.opsForValue().increment(reviewCountKey, 1);
        redisTemplate.opsForValue().increment(scoreSumKey, score);
    }

    public Map<String, Long> getReviewData(Long productId) {
        String reviewCountKey = "product:" + productId + ":reviewCount";
        String scoreSumKey = "product:" + productId + ":scoreSum";

        // Redis에서 데이터 가져오기
        Long reviewCount = redisTemplate.opsForValue().get(reviewCountKey) != null
                ? Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(reviewCountKey)))
                : null;

        Long scoreSum = redisTemplate.opsForValue().get(scoreSumKey) != null
                ? Long.parseLong(Objects.requireNonNull(redisTemplate.opsForValue().get(scoreSumKey)))
                : null;

        // 결과를 Map 형태로 반환
        Map<String, Long> reviewData = new HashMap<>();
        reviewData.put("reviewCount", reviewCount);
        reviewData.put("scoreSum", scoreSum);

        return reviewData;
    }
}

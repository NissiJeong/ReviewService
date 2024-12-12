package com.task.review.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRepository {
    private RedisTemplate<String, String> redisTemplate;

    public void incrementReviewData(Long productId, int score) {
        String reviewCountKey = "product:"+productId+":reviewCount";
        String scoreSumKey = "product:"+productId+":scoreSum";

        redisTemplate.opsForValue().increment(reviewCountKey, 1);
        redisTemplate.opsForValue().increment(scoreSumKey, score);
    }
}

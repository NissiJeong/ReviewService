package com.task.review.repository;

import com.task.review.entity.Product;
import com.task.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductIdAndIdLessThanOrderByIdDesc(Long productId, Long cursorId, Pageable pageable);

    Boolean existsByProductIdAndIdGreaterThan(Long productId, Long id);
}

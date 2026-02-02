package com.kuit.chozy.review.repository;

import com.kuit.chozy.review.domain.Review;
import com.kuit.chozy.review.domain.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByProductIdAndUserIdAndStatusNot(Long productId, Long userId, ReviewStatus status);
}
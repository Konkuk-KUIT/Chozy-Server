package com.kuit.chozy.review.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.review.domain.Review;
import com.kuit.chozy.review.domain.ReviewStatus;
import com.kuit.chozy.review.dto.ReviewCreateRequest;
import com.kuit.chozy.review.repository.ReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    public String createReview(Long userId, ReviewCreateRequest request) {

        validateCreateRequest(userId, request);

        boolean exists = reviewRepository.existsByProductIdAndUserIdAndStatusNot(
                request.getProductId(),
                userId,
                ReviewStatus.DELETED
        );

        if (exists) {
            // 한 상품에 리뷰 1개만 게시 가능인가..?
            throw new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = new Review(
                request.getProductId(),
                userId,
                request.getProductUrl(),
                request.getRating(),
                request.getContent()
        );

        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        return "리뷰를 성공적으로 게시했어요.";
    }

    private void validateCreateRequest(Long userId, ReviewCreateRequest request) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_REQUEST);
        }

        if (request.getProductId() == null || request.getProductId() <= 0) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_REQUEST);
        }

        if (request.getRating() == null) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_REQUEST);
        }

        BigDecimal rating = request.getRating();

        if (rating.compareTo(BigDecimal.ZERO) < 0
                || rating.compareTo(new BigDecimal("5.0")) > 0) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_REQUEST);
        }

        if (rating.scale() > 1) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_REQUEST);
        }

        String sourceLink = request.getProductUrl();
        if (sourceLink != null && sourceLink.length() > 2048) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_REQUEST);
        }
    }

    // ReviewService.java 내부에 추가

    @Transactional
    public String updateReview(Long userId, Long reviewId, com.kuit.chozy.review.dto.ReviewUpdateRequest request) {

        validateUpdateRequest(userId, reviewId, request);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ApiException(ErrorCode.REVIEW_NOT_FOUND));

        try {
            review.validateUpdatable(userId);
        } catch (IllegalStateException e) {
            String reason = e.getMessage();

            if ("UNAUTHORIZED".equals(reason)) {
                throw new ApiException(ErrorCode.UNAUTHORIZED);
            }
            if ("FORBIDDEN".equals(reason)) {
                throw new ApiException(ErrorCode.REVIEW_UPDATE_FORBIDDEN);
            }
            if ("DELETED".equals(reason)) {
                throw new ApiException(ErrorCode.REVIEW_ALREADY_DELETED);
            }

            throw e;
        }

        // productUrl -> sourceLink로 업데이트
        review.update(
                request.getProductUrl(),
                request.getRating(),
                request.getContent()
        );

        // dirty checking으로 자동 반영 (save 호출 없어도 됨)
        return "리뷰를 성공적으로 수정했어요.";
    }

    private void validateUpdateRequest(Long userId, Long reviewId, com.kuit.chozy.review.dto.ReviewUpdateRequest request) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (reviewId == null || reviewId <= 0) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_UPDATE_REQUEST);
        }

        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_UPDATE_REQUEST);
        }

        // PATCH라서 null 허용
        // 단, rating이 들어온 경우에만 검증
        if (request.getRating() != null) {
            BigDecimal rating = request.getRating();

            if (rating.compareTo(BigDecimal.ZERO) < 0
                    || rating.compareTo(new BigDecimal("5.0")) > 0) {
                throw new ApiException(ErrorCode.INVALID_REVIEW_UPDATE_REQUEST);
            }

            if (rating.scale() > 1) {
                throw new ApiException(ErrorCode.INVALID_REVIEW_UPDATE_REQUEST);
            }
        }

        // productUrl이 들어온 경우에만 길이 검증
        if (request.getProductUrl() != null && request.getProductUrl().length() > 2048) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_UPDATE_REQUEST);
        }

        // content가 들어온 경우에만 검증(빈 문자열 금지 정책이면)
        if (request.getContent() != null && request.getContent().trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REVIEW_UPDATE_REQUEST);
        }
    }

}
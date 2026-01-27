package com.kuit.chozy.review.controller;

import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.review.dto.ReviewCreateRequest;
import com.kuit.chozy.review.service.ReviewService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody ReviewCreateRequest request) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        String result = reviewService.createReview(meId, request);
        return ApiResponse.success(result);
    }
}
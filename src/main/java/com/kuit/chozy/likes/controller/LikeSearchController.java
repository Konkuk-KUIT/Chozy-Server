package com.kuit.chozy.likes.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.likes.dto.request.SaveSearchKeywordRequest;
import com.kuit.chozy.likes.dto.response.RecentSearchKeywordResponse;
import com.kuit.chozy.likes.dto.response.RecommendSearchKeywordResponse;
import com.kuit.chozy.likes.service.LikeSearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/likes/search")
public class LikeSearchController {

    private final LikeSearchService likeSearchService;

    // 최근 검색어 조회
    @GetMapping("/recent")
    public ApiResponse<RecentSearchKeywordResponse> getRecentSearchKeyword(
            @UserId Long userId
    ){
        return ApiResponse.success(likeSearchService.getRecentSearchKeyword(userId));
    }

    // 검색어 자동 완성
    @GetMapping("/recommend")
    public ApiResponse<RecommendSearchKeywordResponse> getRecommendSearchKeyword(
            @RequestParam String keyword
    ){
        return ApiResponse.success(likeSearchService.getRecommendSearchKeyword(keyword));
    }

    // 검색어 저장
    @PostMapping
    public ApiResponse<Void> saveSearchKeyword(
            @UserId Long userId,
            @RequestBody SaveSearchKeywordRequest request
    ){
        likeSearchService.saveSearchKeyword(userId, request.keyword());
        return ApiResponse.success(null);
    }

    // 특정 최근 검색어 삭제
    @DeleteMapping("/recent/{keywordId}")
    public ApiResponse<Void> deleteRecentSearchKeyword(
            @UserId Long userId,
            @PathVariable("keywordId") Long keywordId
    ){
        likeSearchService.deleteRecentSearchKeyword(userId, keywordId);
        return ApiResponse.success(null);
    }

    // 최근 검색어 전체 삭제
    @DeleteMapping("/recent")
    public ApiResponse<Void> deleteAllRecentSearchKeywords(
            @UserId Long userId
    ){
        likeSearchService.deleteAllRecentSearchKeywords(userId);
        return ApiResponse.success(null);
    }
}

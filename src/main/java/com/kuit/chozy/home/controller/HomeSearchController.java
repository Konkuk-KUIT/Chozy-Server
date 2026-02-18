package com.kuit.chozy.home.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.home.dto.request.SaveSearchKeywordRequest;
import com.kuit.chozy.home.dto.response.PopularSearchKeywordResponse;
import com.kuit.chozy.home.dto.response.RecentSearchKeywordResponse;
import com.kuit.chozy.home.dto.response.RecommendSearchKeywordResponse;
import com.kuit.chozy.home.service.HomeSearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/home/searches")
@RequiredArgsConstructor
public class HomeSearchController {

    private final HomeSearchService homeSearchService;

    // 최근 검색어 조회
    @GetMapping("/recent")
    public ApiResponse<RecentSearchKeywordResponse> getRecentSearchKeyword(
            @UserId Long userId
    ){
        return ApiResponse.success(homeSearchService.getRecentSearchKeyword(userId));
    }

    // 인기 검색어 조회
    @GetMapping("/popular")
    public ApiResponse<List<PopularSearchKeywordResponse>> getPopularSearchKeyword(){
        return ApiResponse.success(homeSearchService.getPopularSearchKeyword());
    }

    // 검색어 자동 완성
    @GetMapping("/recommend")
    public ApiResponse<RecommendSearchKeywordResponse> getRecommendSearchKeyword(
            @RequestParam String keyword
    ){
        return ApiResponse.success(homeSearchService.getRecommendSearchKeyword(keyword));
    }

    // 검색어 저장
    @PostMapping
    public ApiResponse<Void> saveSearchKeyword(
            @UserId Long userId,
            @RequestBody SaveSearchKeywordRequest request
    ){
        System.out.println("### saveSearchKeyword called ###");
        System.out.println("userId=" + userId + ", keyword=" + request.keyword());

        if (request == null || request.keyword() == null || request.keyword().isBlank()) {
            throw new IllegalArgumentException("keyword is required");
        }
        homeSearchService.saveSearchKeyword(userId, request.keyword());
        return ApiResponse.success(null);
    }

    // 특정 최근 검색어 삭제
    @DeleteMapping("/recent/{keywordId}")
    public ApiResponse<Void> deleteRecentSearchKeyword(
            @UserId Long userId,
            @PathVariable("keywordId") Long keywordId
    ){
        homeSearchService.deleteRecentSearchKeyword(userId, keywordId);
        return ApiResponse.success(null);
    }

    // 최근 검색어 전체 삭제
    @DeleteMapping("/recent")
    public ApiResponse<Void> deleteAllRecentSearchKeywords(
            @UserId Long userId
    ){
        homeSearchService.deleteAllRecentSearchKeywords(userId);
        return ApiResponse.success(null);
    }
}

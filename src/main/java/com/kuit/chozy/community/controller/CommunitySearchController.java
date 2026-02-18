package com.kuit.chozy.community.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.community.dto.request.SaveProfileRequest;
import com.kuit.chozy.community.dto.response.RecentViewedProfileResponse;
import com.kuit.chozy.community.dto.response.UserLoginIdRecommendResponse;
import com.kuit.chozy.community.service.CommunitySearchService;
import com.kuit.chozy.home.dto.request.SaveSearchKeywordRequest;
import com.kuit.chozy.home.dto.response.RecentSearchKeywordResponse;
import com.kuit.chozy.home.dto.response.RecommendSearchKeywordResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/community/searches")
@RequiredArgsConstructor
public class CommunitySearchController {

    private final CommunitySearchService communitySearchService;

    // 최근 검색어 조회
    @GetMapping("/recent")
    public ApiResponse<RecentSearchKeywordResponse> getRecentSearchKeyword(
            @UserId Long userId
    ){
        return ApiResponse.success(communitySearchService.getRecentSearchKeyword(userId));
    }

    // 검색어 자동 완성
    @GetMapping("/recommend")
    public ApiResponse<RecommendSearchKeywordResponse> getRecommendSearchKeyword(
            @RequestParam String keyword
    ){
        return ApiResponse.success(communitySearchService.getRecommendSearchKeyword(keyword));
    }

    // 검색어 저장
    @PostMapping
    public ApiResponse<Void> saveSearchKeyword(
            @UserId Long userId,
            @RequestBody SaveSearchKeywordRequest request
    ){
        communitySearchService.saveSearchKeyword(userId, request.keyword());
        return ApiResponse.success(null);
    }

    // 아이디 자동 완성
    @GetMapping("/recommend/users")
    public ApiResponse<List<UserLoginIdRecommendResponse>> getRecommendLoginId(
            @RequestParam String loginId
    ){
        return ApiResponse.success(communitySearchService.getRecommendLoginId(loginId));
    }

    // 프로필 방문 기록 저장
    @PostMapping("/profile")
    public ApiResponse<Void> saveProfile(
            @UserId Long userId,
            @RequestBody SaveProfileRequest request
    ){
        communitySearchService.saveProfile(userId, request.profileId());
        return ApiResponse.success(null);
    }

    // 최근 프로필 조회 기능
    @GetMapping("/profile")
    public ApiResponse<List<RecentViewedProfileResponse>> getRecentProfiles(
            @UserId Long userId
    ){
        return ApiResponse.success(communitySearchService.getRecentProfiles(userId));
    }

    // 특정 최근 검색어 삭제
    @DeleteMapping("/recent/{keywordId}")
    public ApiResponse<Void> deleteRecentSearchKeyword(
            @UserId Long userId,
            @PathVariable("keywordId") Long keywordId
    ){
        communitySearchService.deleteRecentSearchKeyword(userId, keywordId);
        return ApiResponse.success(null);
    }

    // 최근 검색어 전체 삭제
    @DeleteMapping("/recent")
    public ApiResponse<Void> deleteAllRecentSearchKeywords(
            @UserId Long userId
    ){
        communitySearchService.deleteAllRecentSearchKeywords(userId);
        return ApiResponse.success(null);
    }


}

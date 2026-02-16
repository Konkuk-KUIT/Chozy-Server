package com.kuit.chozy.me.controller;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.me.dto.response.MeBookmarksPageResponse;
import com.kuit.chozy.me.dto.response.MeReviewsPageResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.service.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@Slf4j
@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (!authorization.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorization.substring(7); // "Bearer " 제거

        try {
            return jwtUtil.getUserId(token);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponseDto> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest request
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getMyProfile(userId)
        );
    }

    @PatchMapping("/profile")
    public ApiResponse<ProfileResponseDto> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ProfileUpdateDto request
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.updateMyProfile(userId, request)
        );
    }

    /**
     * 내 후기 목록 (페이지네이션)
     * GET /me/reviews?page=0&size=20&sort=latest
     */
    @GetMapping("/reviews")
    public ApiResponse<MeReviewsPageResponse> getMyReviews(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getMyReviews(userId, page, size, sort)
        );
    }

    /**
     * 내 후기 검색 (내용/키워드)
     * GET /me/reviews/searches?keyword=kwd&page=0&size=20&sort=latest
     */
    @GetMapping("/reviews/searches")
    public ApiResponse<MeReviewsPageResponse> searchMyReviews(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.searchMyReviews(userId, keyword, page, size, sort)
        );
    }

    /**
     * 내 북마크 목록 (페이지네이션)
     * GET /me/bookmarks?page=0&size=20
     */
    @GetMapping("/bookmarks")
    public ApiResponse<MeBookmarksPageResponse> getMyBookmarks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getMyBookmarks(userId, page, size)
        );
    }
}
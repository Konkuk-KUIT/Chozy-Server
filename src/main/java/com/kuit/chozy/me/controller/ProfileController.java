package com.kuit.chozy.me.controller;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.me.dto.response.BookmarkListResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.dto.response.ReviewListResponse;
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

    // TODO: JWT token에서 loginId 추출
    private String extractLoginId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return authorization;
    }

    @GetMapping("/profile")
    public ApiResponse<ProfileResponseDto> getMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest request
    ) {
        String loginId = extractLoginId(authorization);
        return ApiResponse.success(
                profileService.getMyProfile(loginId)
        );
    }

    @PatchMapping("/profile")
    public ApiResponse<ProfileResponseDto> updateMyProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ProfileUpdateDto request
    ) {
        String loginId = extractLoginId(authorization);
        return ApiResponse.success(
                profileService.updateMyProfile(loginId, request)
        );
    }

    @GetMapping("/reviews")
    public ApiResponse<ReviewListResponse> getMyReviews(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String loginId = extractLoginId(authorization);
        return ApiResponse.success(
                profileService.getMyReviews(loginId, page, size)
        );
    }

    @GetMapping("/reviews/searches")
    public ApiResponse<ReviewListResponse> searchMyReviews(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String loginId = extractLoginId(authorization);
        return ApiResponse.success(
                profileService.searchMyReviews(loginId, keyword, page, size)
        );
    }

    @GetMapping("/bookmarks")
    public ApiResponse<BookmarkListResponse> getMyBookmarks(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String loginId = extractLoginId(authorization);
        return ApiResponse.success(
                profileService.getMyBookmarks(loginId, page, size)
        );
    }
}
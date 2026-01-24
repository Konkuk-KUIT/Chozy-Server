package com.kuit.chozy.me.controller;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.me.dto.response.BookmarkListResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.dto.response.ReviewListResponse;
import com.kuit.chozy.me.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public ApiResponse<ProfileResponseDto> getMyProfile(
            // TODO: 로그인 기능 생기면 JWT 토큰에서 loginId 추출
            @RequestHeader(value = "X-LOGIN-ID", required = false) String loginId
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                profileService.getMyProfile(loginId)
        );
    }

    @PatchMapping("/profile")
    public ApiResponse<ProfileResponseDto> updateMyProfile(
            // TODO: 로그인 기능 생기면 JWT 토큰에서 loginId 추출
            @RequestHeader(value = "X-LOGIN-ID", required = false) String loginId,
            @RequestBody ProfileUpdateDto request
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                profileService.updateMyProfile(loginId, request)
        );
    }

    @GetMapping("/reviews")
    public ApiResponse<ReviewListResponse> getMyReviews(
            // TODO: 로그인 기능 생기면 JWT 토큰에서 loginId 추출
            @RequestHeader(value = "X-LOGIN-ID", required = false) String loginId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                profileService.getMyReviews(loginId, page, size)
        );
    }

    @GetMapping("/reviews/searches")
    public ApiResponse<ReviewListResponse> searchMyReviews(
            // TODO: 로그인 기능 생기면 JWT 토큰에서 loginId 추출
            @RequestHeader(value = "X-LOGIN-ID", required = false) String loginId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                profileService.searchMyReviews(loginId, keyword, page, size)
        );
    }

    @GetMapping("/bookmarks")
    public ApiResponse<BookmarkListResponse> getMyBookmarks(
            // TODO: 로그인 기능 생기면 JWT 토큰에서 loginId 추출
            @RequestHeader(value = "X-LOGIN-ID", required = false) String loginId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (loginId == null || loginId.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        return ApiResponse.success(
                profileService.getMyBookmarks(loginId, page, size)
        );
    }
}
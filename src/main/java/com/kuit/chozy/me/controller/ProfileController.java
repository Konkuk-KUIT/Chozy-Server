package com.kuit.chozy.me.controller;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.me.dto.response.*;
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
     * 내 피드 목록 (페이지네이션)
     * GET /me/feeds?page=0&size=20&sort=latest
     */
    @GetMapping("/feeds")
    public ApiResponse<MeFeedsPageResponse> getMyFeeds(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getMyFeeds(userId, page, size, sort)
        );
    }

    /**
     * 내 피드 검색 (내용/키워드)
     * GET /me/feeds/searches?query=kwd&page=0&size=20&sort=latest
     */
    @GetMapping("/feeds/searches")
    public ApiResponse<MeFeedsPageResponse> searchMyFeeds(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.searchMyFeeds(userId, query, page, size, sort)
        );
    }

    /**
     * 좋아요한 게시글 목록
     * GET /me/feeds/liked?page=0&size=20&sort=latest
     */
    @GetMapping("/feeds/liked")
    public ApiResponse<MeLikedFeedsPageResponse> getMyLikedFeeds(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getMyLikedFeeds(userId, page, size, sort)
        );
    }

    /**
     * 최근 검색어 조회
     * GET /me/searches/recent
     */
    @GetMapping("/searches/recent")
    public ApiResponse<RecentSearchesResponse> getRecentSearches(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getRecentSearches(userId)
        );
    }

    /**
     * 마이피드 내 검색
     * GET /me/searches?query=str&page=0&size=20
     */
    @GetMapping("/searches")
    public ApiResponse<MeSearchResultResponse> searchMyFeedsInMe(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.searchMyFeedsForMe(userId, query, page, size)
        );
    }

    /**
     * 검색 기록 삭제
     * DELETE /me/searches/{searched_id}
     */
    @DeleteMapping("/searches/{searchedId}")
    public ApiResponse<DeleteSearchResponse> deleteSearch(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long searchedId
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.deleteSearch(userId, searchedId)
        );
    }

    /**
     * 검색 기록 전체 삭제
     * DELETE /me/searches
     */
    @DeleteMapping("/searches")
    public ApiResponse<DeleteAllSearchesResponse> deleteAllSearches(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        Long userId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.deleteAllSearches(userId)
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
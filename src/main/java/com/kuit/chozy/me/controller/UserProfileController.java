package com.kuit.chozy.me.controller;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.me.dto.response.MeFeedsPageResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.me.service.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    private Long extractUserId(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        if (!authorization.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
        String token = authorization.substring(7);
        try {
            return jwtUtil.getUserId(token);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 타인 프로필 조회
     * GET /users/{targetUserId}/profile
     */
    @GetMapping("/{targetUserId}/profile")
    public ApiResponse<ProfileResponseDto> getTargetProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long targetUserId
    ) {
        Long viewerId = extractUserId(authorization);
        return ApiResponse.success(profileService.getTargetProfile(viewerId, targetUserId));
    }

    /**
     * 타인 피드 목록 (페이지네이션)
     * GET /users/{targetUserId}/feeds?page=0&size=20&sort=latest
     */
    @GetMapping("/{targetUserId}/feeds")
    public ApiResponse<MeFeedsPageResponse> getTargetFeeds(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long viewerId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.getTargetFeeds(viewerId, targetUserId, page, size, sort)
        );
    }

    /**
     * 타인 피드 검색 (내용/키워드)
     * GET /users/{targetUserId}/feeds/searches?query=kwd&page=0&size=20&sort=latest
     */
    @GetMapping("/{targetUserId}/feeds/searches")
    public ApiResponse<MeFeedsPageResponse> searchTargetFeeds(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long targetUserId,
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Long viewerId = extractUserId(authorization);
        return ApiResponse.success(
                profileService.searchTargetFeeds(viewerId, targetUserId, query, page, size, sort)
        );
    }
}

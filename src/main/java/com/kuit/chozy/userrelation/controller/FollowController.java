package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.response.FollowActionResponse;
import com.kuit.chozy.userrelation.dto.response.FollowerListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowingListResponse;
import com.kuit.chozy.userrelation.service.FollowListService;
import com.kuit.chozy.userrelation.service.FollowService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class FollowController {

    private final FollowService followService;
    private final FollowListService followListService;

    public FollowController(FollowService followService, FollowListService followListService) {
        this.followService = followService;
        this.followListService = followListService;
    }

    @PostMapping("/me/followings/{targetUserId}")
    public ApiResponse<FollowActionResponse> follow(@PathVariable Long targetUserId) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        FollowActionResponse result = followService.follow(meId, targetUserId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/me/followings/{targetUserId}")
    public ApiResponse<FollowActionResponse> unfollowOrCancel(@PathVariable Long targetUserId) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        FollowActionResponse result = followService.unfollowOrCancel(meId, targetUserId);
        return ApiResponse.success(result);
    }

    // 팔로워 목록 조회
    @GetMapping("/me/followers")
    public ApiResponse<FollowerListResponse> getFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        FollowerListResponse result = followListService.getFollowers(meId, page, size);
        return ApiResponse.success(result);
    }

    // 팔로잉 목록 조회
    @GetMapping("/me/followings")
    public ApiResponse<FollowingListResponse> getFollowings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        FollowingListResponse result = followListService.getFollowings(meId, page, size);
        return ApiResponse.success(result);
    }

    private Long resolveMeId(String authorization) {
        // TODO: 나중에 JWT 붙이면 여기서 파싱해서 meId 반환
        // 지금은 임시: Authorization 없으면 1L
        if (authorization == null || authorization.isBlank()) {
            return 1L;
        }
        return 1L; // 임시
    }

}
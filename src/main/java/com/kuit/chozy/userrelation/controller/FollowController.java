package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
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
    public ApiResponse<FollowActionResponse> follow(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(followService.follow(userId, targetUserId));
    }

    @DeleteMapping("/me/followings/{targetUserId}")
    public ApiResponse<FollowActionResponse> unfollowOrCancel(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(followService.unfollowOrCancel(userId, targetUserId));
    }

    // 팔로워 목록 조회
    @GetMapping("/me/followers")
    public ApiResponse<FollowerListResponse> getFollowers(
            @UserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(followListService.getFollowers(userId, page, size));
    }

    // 팔로잉 목록 조회
    @GetMapping("/me/followings")
    public ApiResponse<FollowingListResponse> getFollowings(
            @UserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(followListService.getFollowings(userId, page, size));
    }
}
package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.request.FollowRequestProcessRequest;
import com.kuit.chozy.userrelation.dto.response.FollowRequestListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowRequestProcessResponse;
import com.kuit.chozy.userrelation.service.FollowRequestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class FollowRequestController {

    private final FollowRequestService followRequestService;

    public FollowRequestController(FollowRequestService followRequestService) {
        this.followRequestService = followRequestService;
    }

    // 팔로우 요청 수락/거절
    @PatchMapping("/follow-requests/{requestId}")
    public ApiResponse<FollowRequestProcessResponse> process(
            @UserId Long userId,
            @PathVariable Long requestId,
            @RequestBody FollowRequestProcessRequest request
    ) {
        return ApiResponse.success(
                followRequestService.process(userId, requestId, request.getStatus())
        );
    }

    // 내 팔로우 요청 목록 조회
    @GetMapping("/me/follow-requests")
    public ApiResponse<FollowRequestListResponse> list(
            @UserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                followRequestService.getMyPendingRequests(userId, page, size)
        );
    }
}
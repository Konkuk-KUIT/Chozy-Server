package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.request.FollowRequestProcessRequest;
import com.kuit.chozy.userrelation.dto.response.FollowRequestListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowRequestProcessResponse;
import com.kuit.chozy.userrelation.service.FollowRequestService;
import org.springframework.web.bind.annotation.*;

@RestController
public class FollowRequestController {

    private final FollowRequestService followRequestService;

    public FollowRequestController(FollowRequestService followRequestService) {
        this.followRequestService = followRequestService;
    }

    @PatchMapping("/users/follow-requests/{requestId}")
    public ApiResponse<FollowRequestProcessResponse> process(
            @PathVariable Long requestId,
            @RequestBody FollowRequestProcessRequest request
            // meUserId 주입: 프로젝트 인증 방식으로 교체
    ) {
        Long meUserId = 1L; // TODO: 인증 연동

        FollowRequestProcessResponse result =
                followRequestService.process(meUserId, requestId, request.getStatus());

        return ApiResponse.success(result);
    }

    @GetMapping("/users/me/follow-requests")
    public ApiResponse<FollowRequestListResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            // meUserId 주입: 프로젝트 인증 방식으로 교체
    ) {
        Long meUserId = 1L; // TODO: 인증 연동

        FollowRequestListResponse result = followRequestService.getMyPendingRequests(meUserId, page, size);
        return ApiResponse.success(result);
    }
}
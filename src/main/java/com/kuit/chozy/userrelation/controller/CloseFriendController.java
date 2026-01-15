package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendListResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendSetResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendUnsetResponse;
import com.kuit.chozy.userrelation.service.CloseFriendService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/close-friends")
public class CloseFriendController {

    private final CloseFriendService closeFriendService;

    public CloseFriendController(CloseFriendService closeFriendService) {
        this.closeFriendService = closeFriendService;
    }

    @PostMapping("/{targetUserId}")
    public ApiResponse<CloseFriendSetResponse> setCloseFriend(
            @PathVariable Long targetUserId
            // 여기에 meId 주입
            // 예: @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // TODO: accessToken 기반으로 meId 추출
        // Long meId = userDetails.getUserId();
        Long meId = 1L;

        CloseFriendSetResponse result = closeFriendService.setCloseFriend(meId, targetUserId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{targetUserId}")
    public ApiResponse<CloseFriendUnsetResponse> unsetCloseFriend(
            @PathVariable Long targetUserId
    ) {
        // Long meId = userDetails.getUserId();
        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        CloseFriendUnsetResponse result = closeFriendService.unsetCloseFriend(meId, targetUserId);
        return ApiResponse.success(result);
    }

    @GetMapping
    public ApiResponse<CloseFriendListResponse> getCloseFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            // TODO: accessToken 기반으로 meId 추출
    ) {
        // Long meId = userDetails.getUserId();
        Long meId = 1L;

        CloseFriendListResponse result = closeFriendService.getCloseFriends(meId, page, size);
        return ApiResponse.success(result);
    }

}

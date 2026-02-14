package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendListResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendSetResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendUnsetResponse;
import com.kuit.chozy.userrelation.service.CloseFriendService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/users/me/close-friends")
public class CloseFriendController {

    private final CloseFriendService closeFriendService;

    public CloseFriendController(CloseFriendService closeFriendService) {
        this.closeFriendService = closeFriendService;
    }

    @PostMapping("/{targetUserId}")
    public ApiResponse<CloseFriendSetResponse> setCloseFriend(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(
                closeFriendService.setCloseFriend(userId, targetUserId)
        );
    }

    @DeleteMapping("/{targetUserId}")
    public ApiResponse<CloseFriendUnsetResponse> unsetCloseFriend(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(
                closeFriendService.unsetCloseFriend(userId, targetUserId)
        );
    }

    @GetMapping
    public ApiResponse<CloseFriendListResponse> getCloseFriends(
            @UserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(
                closeFriendService.getCloseFriends(userId, page, size)
        );
    }
}

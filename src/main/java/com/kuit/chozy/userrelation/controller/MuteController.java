package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.response.MuteResponse;
import com.kuit.chozy.userrelation.dto.response.MutedUserListResponse;
import com.kuit.chozy.userrelation.dto.response.UnmuteResponse;
import com.kuit.chozy.userrelation.service.MuteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/users")
public class MuteController {

    private final MuteService muteService;

    public MuteController(MuteService muteService) {
        this.muteService = muteService;
    }

    // 관심없음 설정 (상대방은 알 수 없음)
    @PostMapping("/me/mutes/{targetUserId}")
    public ApiResponse<MuteResponse> mute(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(muteService.mute(userId, targetUserId));
    }

    // 관심없음 해제
    @DeleteMapping("/me/mutes/{targetUserId}")
    public ApiResponse<UnmuteResponse> unmute(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(muteService.unmute(userId, targetUserId));
    }

    // 관심없음 목록 조회
    @GetMapping("/me/mutes")
    public ApiResponse<MutedUserListResponse> getMutedUsers(
            @UserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(muteService.getMutedUsers(userId, page, size));
    }
}

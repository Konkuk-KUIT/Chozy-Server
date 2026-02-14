package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.userrelation.dto.response.BlockResponse;
import com.kuit.chozy.userrelation.dto.response.BlockedUserListResponse;
import com.kuit.chozy.userrelation.dto.response.UnblockResponse;
import com.kuit.chozy.userrelation.service.BlockService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class BlockController {

    private final BlockService blockService;

    public BlockController(BlockService blockService) {
        this.blockService = blockService;
    }

    // 차단
    @PostMapping("/me/blocks/{targetUserId}")
    public ApiResponse<BlockResponse> block(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(blockService.block(userId, targetUserId));
    }

    // 차단 해제
    @DeleteMapping("/me/blocks/{targetUserId}")
    public ApiResponse<UnblockResponse> unblock(
            @UserId Long userId,
            @PathVariable Long targetUserId
    ) {
        return ApiResponse.success(blockService.unblock(userId, targetUserId));
    }

    // 차단 목록 조회
    @GetMapping("/me/blocks")
    public ApiResponse<BlockedUserListResponse> getBlockedUsers(
            @UserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success(blockService.getBlockedUsers(userId, page, size));
    }
}
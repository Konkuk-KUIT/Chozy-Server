package com.kuit.chozy.userrelation.controller;

import com.kuit.chozy.common.response.ApiResponse;
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
    public ApiResponse<BlockResponse> block(@PathVariable Long targetUserId) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        BlockResponse result = blockService.block(meId, targetUserId);
        return ApiResponse.success(result);
    }

    // 차단 해제
    @DeleteMapping("/me/blocks/{targetUserId}")
    public ApiResponse<UnblockResponse> unblock(@PathVariable Long targetUserId) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        UnblockResponse result = blockService.unblock(meId, targetUserId);
        return ApiResponse.success(result);
    }

    // 차단 목록 조회
    @GetMapping("/me/blocks")
    public ApiResponse<BlockedUserListResponse> getBlockedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;
        return ApiResponse.success(blockService.getBlockedUsers(meId, page, size));
    }
}

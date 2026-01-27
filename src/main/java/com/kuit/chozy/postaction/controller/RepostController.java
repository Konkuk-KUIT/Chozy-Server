package com.kuit.chozy.postaction.controller;

import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.postaction.dto.RepostCreateRequest;
import com.kuit.chozy.postaction.service.RepostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/repost")
public class RepostController {

    private final RepostService repostService;

    public RepostController(RepostService repostService) {
        this.repostService = repostService;
    }

    @PostMapping
    public ApiResponse<String> repost(@RequestBody RepostCreateRequest request) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        String result = repostService.repost(meId, request);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{feedId}")
    public ApiResponse<String> cancel(@PathVariable Long feedId) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        String result = repostService.cancelRepost(meId, feedId);
        return ApiResponse.success(result);
    }
}
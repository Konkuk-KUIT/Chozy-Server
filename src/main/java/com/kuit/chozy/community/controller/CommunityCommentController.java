package com.kuit.chozy.community.controller;

import com.kuit.chozy.community.dto.request.CommentReactionRequest;
import com.kuit.chozy.community.service.CommunityFeedService;
import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/community/comments")
@RequiredArgsConstructor
public class CommunityCommentController {

    private final CommunityFeedService communityFeedService;

    /**
     * 댓글 좋아요/싫어요
     */
    @PostMapping("/{commentId}/reactions")
    public ApiResponse<String> setCommentReaction(
            @UserId Long userId,
            @PathVariable Long commentId,
            @RequestBody CommentReactionRequest request
    ) {
        if (request.getLike() == null) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        communityFeedService.setCommentReaction(commentId, userId, request.getLike());
        return ApiResponse.success("성공적으로 처리됐습니다.");
    }
}

package com.kuit.chozy.community.controller;

import com.kuit.chozy.community.dto.request.CommentCreateRequest;
import com.kuit.chozy.community.dto.request.CommentReactionRequest;
import com.kuit.chozy.community.dto.request.FeedBookmarkRequest;
import com.kuit.chozy.community.dto.request.FeedReactionRequest;
import com.kuit.chozy.community.dto.response.CommentCreateResponse;
import com.kuit.chozy.community.dto.response.FeedDetailResponse;
import com.kuit.chozy.community.dto.response.FeedItemResponse;
import com.kuit.chozy.community.domain.FeedTab;
import com.kuit.chozy.community.service.CommunityFeedService;
import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community/feeds")
@RequiredArgsConstructor
public class CommunityFeedController {

    private final CommunityFeedService communityFeedService;

    /**
     * 피드 목록 조회
     */
    @GetMapping
    public ApiResponse<List<FeedItemResponse>> getFeeds(
            @UserId Long userId,
            @RequestParam(defaultValue = "RECOMMEND") FeedTab tab,
            @RequestParam(defaultValue = "ALL") String contentType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<FeedItemResponse> result = communityFeedService.getFeeds(userId, tab, contentType, search, page, size);
        return ApiResponse.success(result);
    }

    /**
     * 게시글 상세 조회 (조회수 +1)
     */
    @GetMapping("/{feedId}/detail")
    public ApiResponse<FeedDetailResponse> getFeedDetail(
            @UserId Long userId,
            @PathVariable Long feedId
    ) {
        FeedDetailResponse result = communityFeedService.getFeedDetail(feedId, userId);
        return ApiResponse.success(result);
    }

    /**
     * 게시글 좋아요/싫어요
     */
    @PostMapping("/{feedId}/reactions")
    public ApiResponse<String> setFeedReaction(
            @UserId Long userId,
            @PathVariable Long feedId,
            @RequestBody FeedReactionRequest request
    ) {
        if (request.getLike() == null) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        communityFeedService.setFeedReaction(feedId, userId, request.getLike());
        return ApiResponse.success("성공적으로 처리됐습니다.");
    }

    /**
     * 게시글 북마크 (true: 추가, false: 취소)
     */
    @PostMapping("/{feedId}/bookmarks")
    public ApiResponse<String> setFeedBookmark(
            @UserId Long userId,
            @PathVariable Long feedId,
            @RequestBody FeedBookmarkRequest request
    ) {
        if (request.getBookmark() == null) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        communityFeedService.setFeedBookmark(feedId, userId, request.getBookmark());
        return ApiResponse.success("성공적으로 처리됐습니다.");
    }

    /**
     * 게시글 삭제 (본인만 가능)
     */
    @DeleteMapping("/{feedId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeed(
            @UserId Long userId,
            @PathVariable Long feedId
    ) {
        communityFeedService.deleteFeed(feedId, userId);
    }

    /**
     * 댓글 작성 (parentCommentId 없으면 null = 최상위 댓글)
     */
    @PostMapping("/{feedId}/comments")
    public ApiResponse<CommentCreateResponse> createComment(
            @UserId Long userId,
            @PathVariable Long feedId,
            @RequestBody CommentCreateRequest request
    ) {
        CommentCreateResponse result = communityFeedService.createComment(feedId, userId, request);
        return ApiResponse.success(result);
    }
}

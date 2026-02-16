package com.kuit.chozy.community.controller;

import com.kuit.chozy.community.dto.request.*;
import com.kuit.chozy.community.dto.response.CommentCreateResponse;
import com.kuit.chozy.community.dto.response.FeedDetailResponse;
import com.kuit.chozy.community.dto.response.FeedItemResponse;
import com.kuit.chozy.community.domain.FeedTab;
import com.kuit.chozy.community.service.CommunityFeedService;
import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@SecurityRequirement(name = "BearerAuth")
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

    @PostMapping("/{feedId}/repost")
    public ApiResponse<Long> repost(
            @UserId Long userId,
            @PathVariable Long feedId
    ) {
        Long id = communityFeedService.createRepost(userId, feedId);
        return ApiResponse.success(id);
    }

    @DeleteMapping("/{feedId}/repost")
    public ApiResponse<String> cancelRepost(
            @UserId Long userId,
            @PathVariable Long feedId
    ) {
        communityFeedService.cancelRepost(userId, feedId);
        return ApiResponse.success("성공적으로 처리됐습니다.");
    }

    @PostMapping("/{feedId}/quote")
    public ApiResponse<Long> quote(
            @UserId Long userId,
            @PathVariable Long feedId,
            @RequestBody FeedQuoteCreateRequest request
    ) {
        Long id = communityFeedService.createQuote(userId, feedId, request.getText());
        return ApiResponse.success(id);
    }

    @PostMapping("/post")
    public ApiResponse<Long> createPostFeed(
            @UserId Long userId,
            @RequestBody FeedPostCreateRequest request
    ) {
        if (!StringUtils.hasText(request.getContent())) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        List<String> imageUrls = Collections.emptyList();
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            imageUrls = request.getImg().stream()
                    .map(FeedPostCreateRequest.ImageMeta::getFileName)
                    .toList();
        }

        Long id = communityFeedService.createPostFeed(
                userId,
                request.getContent(),
                imageUrls,
                request.getHashTags()
        );
        return ApiResponse.success(id);
    }

    @PostMapping("/review")
    public ApiResponse<Long> createReviewFeed(
            @UserId Long userId,
            @RequestBody FeedReviewCreateRequest request
    ) {
        if (!StringUtils.hasText(request.getContent())) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        Long id = communityFeedService.createReviewFeed(
                userId,
                request.getContent(),
                request.getVendor(),
                request.getRating(),
                request.getProductUrl(),
                Collections.emptyList(),
                null
        );
        return ApiResponse.success(id);
    }

    @PatchMapping("/{feedId}/post")
    public ApiResponse<String> updatePostFeed(
            @UserId Long userId,
            @PathVariable Long feedId,
            @RequestBody FeedPostUpdateRequest request
    ) {
        if (!StringUtils.hasText(request.getContent())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        List<String> imageUrls = Collections.emptyList();
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            imageUrls = request.getImg().stream()
                    .map(FeedPostUpdateRequest.ImageMeta::getFileName)
                    .toList();
        }

        communityFeedService.updatePostFeed(
                feedId,
                userId,
                request.getContent(),
                request.getHashTags(),
                imageUrls
        );

        return ApiResponse.success("수정 완료");
    }

    @PatchMapping("/{feedId}/review")
    public ApiResponse<String> updateReviewFeed(
            @UserId Long userId,
            @PathVariable Long feedId,
            @RequestBody FeedReviewUpdateRequest request
    ) {
        if (!StringUtils.hasText(request.getContent()) || !StringUtils.hasText(request.getVendor())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        List<String> imageUrls = Collections.emptyList();
        if (request.getImg() != null && !request.getImg().isEmpty()) {
            imageUrls = request.getImg().stream()
                    .map(FeedReviewUpdateRequest.ImageMeta::getFileName)
                    .toList();
        }

        communityFeedService.updateReviewFeed(
                feedId,
                userId,
                request.getContent(),
                request.getVendor(),
                request.getRating(),
                request.getProductUrl(),
                request.getHashTags(),
                imageUrls
        );

        return ApiResponse.success("수정 완료");
    }
}

package com.kuit.chozy.community.controller;

import com.kuit.chozy.community.dto.response.FeedItemResponse;
import com.kuit.chozy.community.domain.FeedTab;
import com.kuit.chozy.community.service.CommunityFeedService;
import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community/feeds")
@RequiredArgsConstructor
public class CommunityFeedController {

    private final CommunityFeedService communityFeedService;

    /**
     * 피드 목록 조회
     * @param tab RECOMMEND(추천) | FOLLOWING(팔로우)
     * @param contentType ALL | POST | REVIEW
     * @param search 검색어 (선택)
     * @param page 페이지 (기본 0)
     * @param size 크기 (기본 20, 최대 50)
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
}

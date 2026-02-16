package com.kuit.chozy.me.dto.response;

import com.kuit.chozy.community.domain.FeedContentType;
import com.kuit.chozy.community.domain.FeedKind;
import com.kuit.chozy.community.dto.response.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 북마크 목록용 피드 아이템 (FeedItemResponse + bookmarkedAt)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedBookmarkItemResponse {
    private Long feedId;
    private FeedKind kind;
    private FeedContentType contentType;
    private LocalDateTime createdAt;
    private FeedUserResponse user;
    private FeedListContentResponse contents;
    private FeedCountsResponse counts;
    private FeedMyStateResponse myState;
    private LocalDateTime bookmarkedAt;
}

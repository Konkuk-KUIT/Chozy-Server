package com.kuit.chozy.me.dto.response;

import com.kuit.chozy.community.domain.FeedContentType;
import com.kuit.chozy.community.domain.FeedKind;
import com.kuit.chozy.community.dto.response.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedLikedItemResponse {
    private Long feedId;
    private FeedKind kind;
    private FeedContentType contentType;
    private LocalDateTime createdAt;
    private FeedUserResponse user;
    private FeedListContentResponse contents;
    private FeedCountsResponse counts;
    private FeedMyStateResponse myState;
    private LocalDateTime likedAt;
}

package com.kuit.chozy.community.dto.response;

import com.kuit.chozy.community.domain.FeedContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDetailFeedResponse {
    private Long feedId;
    private FeedContentType contentType;
    private boolean isMine;
    private LocalDateTime createdAt;
    private FeedUserResponse user;
    private FeedContentResponse content;
    private FeedCountsResponse counts;
    private FeedMyStateResponse myState;
}

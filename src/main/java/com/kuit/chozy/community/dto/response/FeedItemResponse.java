package com.kuit.chozy.community.dto.response;

import com.kuit.chozy.community.domain.FeedContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedItemResponse {
    private Long feedId;
    private FeedContentType contentType;
    private FeedUserResponse user;
    private FeedContentResponse content;
    private FeedCountsResponse counts;
    private FeedMyStateResponse myState;
}

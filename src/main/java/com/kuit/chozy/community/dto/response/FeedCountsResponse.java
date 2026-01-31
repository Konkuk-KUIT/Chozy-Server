package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedCountsResponse {
    private Long commentCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long quoteCount;
}

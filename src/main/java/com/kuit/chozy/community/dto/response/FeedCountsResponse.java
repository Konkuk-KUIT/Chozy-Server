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
    private Integer viewCount;   // 조회수 (목록/상세 공통)
    private Integer commentCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer quoteCount;
}
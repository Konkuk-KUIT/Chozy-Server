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
    private Integer views;       // 상세 조회 시에만 사용 (목록에서는 null 가능)
    private Integer commentCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer quoteCount;
}
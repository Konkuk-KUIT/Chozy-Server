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
    private Long views;       // 상세 조회 시에만 사용 (목록에서는 null 가능)
    private Long commentCount;
    private Long likeCount;
    private Long dislikeCount;
    private Long quoteCount;
}

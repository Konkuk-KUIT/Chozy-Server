package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentCountsResponse {
    private Integer commentCount;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer quoteCount;
}
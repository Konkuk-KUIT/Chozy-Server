package com.kuit.chozy.me.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewItemResponse {
    private final Long postId;
    private final String content;
    private final List<String> imageUrls;
    private final Integer likeCount;
    private final Integer dislikeCount;
    private final Integer commentCount;
    private final Integer quoteCount;
    private final LocalDateTime createdAt;
}

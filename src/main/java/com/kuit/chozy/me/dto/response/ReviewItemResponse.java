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
    private final Long likeCount;
    private final Long dislikeCount;
    private final Long commentCount;
    private final Long quoteCount;
    private final LocalDateTime createdAt;
}

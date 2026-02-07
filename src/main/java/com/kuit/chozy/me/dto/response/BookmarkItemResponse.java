package com.kuit.chozy.me.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookmarkItemResponse {
    private final Long postId;
    private final String content;
    private final List<String> imageUrls;
    private final BookmarkAuthorResponse author;
    private final Integer likeCount;
    private final Integer dislikeCount;
    private final Integer commentCount;
    private final Integer quoteCount;

    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    private final LocalDateTime bookmarkedAt;
    private final LocalDateTime createdAt;
}

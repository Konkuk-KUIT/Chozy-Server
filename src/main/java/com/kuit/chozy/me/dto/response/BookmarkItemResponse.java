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
    private final Long likeCount;
    private final Long dislikeCount;
    private final Long commentCount;
    private final Long quoteCount;

    @JsonProperty("isBookmarked")
    private final boolean isBookmarked;

    private final LocalDateTime bookmarkedAt;
    private final LocalDateTime createdAt;
}

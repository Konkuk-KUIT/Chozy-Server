package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedContentResponse {
    // POST: text, contentImgs
    private String text;
    private List<String> contentImgs;

    // REVIEW only
    private String vendor;
    private String productUrl;
    private String title;
    private Float rating;

    // REVIEW with quote (인용)
    private FeedQuoteContentResponse quoteContent;

    // 해시태그 (상세 등)
    private String hashTags;
}

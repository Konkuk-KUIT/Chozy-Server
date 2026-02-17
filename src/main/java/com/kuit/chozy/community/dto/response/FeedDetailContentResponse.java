package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 게시글 상세 조회 시 contents 스키마 (목록과 다름)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedDetailContentResponse {
    private String vendor;
    private String productUrl;
    private String title;
    private BigDecimal rating;
    private String content;
    private List<FeedImageItemResponse> feedImages;
    private List<String> hashTags;
    private FeedQuoteInContentResponse quote;  // kind=QUOTE, REPOST일 때 존재
}

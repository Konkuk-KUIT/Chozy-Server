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
public class FeedListContentResponse {
    private String text;
    private List<FeedImageItemResponse> images;
    private FeedReviewInContentResponse review;  // contentType=REVIEW일 때만
    private FeedQuoteInContentResponse quote;    // kind=QUOTE일 때만
}

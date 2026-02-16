package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedQuoteContentResponse {
    private FeedUserResponse user;
    private String vendor;
    private String title;
    private BigDecimal rating;
    private String text;
    private List<String> contentImgs;
}
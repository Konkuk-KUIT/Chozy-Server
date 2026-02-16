package com.kuit.chozy.community.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedReviewInContentResponse {
    private String vendor;
    private String title;
    private BigDecimal rating;
    private String productUrl;
}

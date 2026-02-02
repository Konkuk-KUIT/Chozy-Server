package com.kuit.chozy.review.dto;

import java.math.BigDecimal;

public class ReviewUpdateRequest {

    private String productUrl;
    private BigDecimal rating;
    private String content;

    public ReviewUpdateRequest() {
    }

    public String getProductUrl() {
        return productUrl;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }
}
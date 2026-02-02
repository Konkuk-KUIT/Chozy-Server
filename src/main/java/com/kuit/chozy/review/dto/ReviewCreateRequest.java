package com.kuit.chozy.review.dto;

import java.math.BigDecimal;

public class ReviewCreateRequest {

    private Long productId;
    private String productUrl;
    private BigDecimal rating;
    private String content;

    public ReviewCreateRequest() {
    }

    public Long getProductId() {
        return productId;
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
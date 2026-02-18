package com.kuit.chozy.home.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoupangProductSearchResponse(
        String rCode,
        String rMessage,
        Data data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String landingUrl,
            List<Item> productData
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            String keyword,
            Integer rank,
            Boolean isRocket,
            Boolean isFreeShipping,
            long productId,
            String productImage,
            String productName,
            double productPrice,
            String productUrl
    ) {}
}

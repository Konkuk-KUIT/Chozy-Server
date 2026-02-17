package com.kuit.chozy.home.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoupangProductCategoryResponse(
        List<String> accessPasses,
        List<Item> data,
        String rCode,
        String rMessage
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            @JsonProperty("isRocket")
            boolean isRocket,

            long productId,
            String productImage,
            String productName,
            double productPrice,
            String productUrl,
            String categoryName,
            String impressionUrl
    ) {}
}

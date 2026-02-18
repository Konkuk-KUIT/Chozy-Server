package com.kuit.chozy.home.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AliExpressProductDto(
        @JsonProperty("product_id") Long productId,
        @JsonProperty("product_title") String productTitle,
        @JsonProperty("product_main_image_url") String productMainImageUrl,
        @JsonProperty("product_detail_url") String productDetailUrl,
        @JsonProperty("promotion_link") String promotionLink,
        @JsonProperty("discount") String discount,
        @JsonProperty("app_sale_price") String appSalePrice,
        @JsonProperty("original_price") String originalPrice,
        @JsonProperty("target_app_sale_price") String targetAppSalePrice,
        @JsonProperty("target_sale_price") String targetSalePrice,
        @JsonProperty("target_original_price") String targetOriginalPrice,
        @JsonProperty("first_level_category_id") Long firstLevelCategoryId,
        @JsonProperty("second_level_category_id") Long secondLevelCategoryId
) {}

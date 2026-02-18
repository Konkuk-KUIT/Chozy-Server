package com.kuit.chozy.likes.dto.response;

import com.kuit.chozy.home.entity.Product;

public record LikeItemResponse(
        Long productId,
        String name,
        Integer originalPrice,
        Integer discountRate,
        String imageUrl,
        String productUrl
) {
    public static LikeItemResponse from(Product p) {
        return new LikeItemResponse(
                p.getId(),
                p.getName(),
                p.getListPrice(),        // 원가/정가가 listPrice라면
                p.getDiscountRate(),
                p.getProductImageUrl(),
                p.getProductUrl()
        );
    }
}

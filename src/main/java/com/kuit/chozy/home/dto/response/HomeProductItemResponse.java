package com.kuit.chozy.home.dto.response;

import com.kuit.chozy.home.entity.Product;

public record HomeProductItemResponse(
        Long productId,
        String name,
        Integer originalPrice,
        Integer discountRate,
        String imageUrl,
        String productUrl,
        boolean isFavorited
) {
    public static HomeProductItemResponse from(Product p, boolean isFavorited) {
        return new HomeProductItemResponse(
                p.getId(),
                p.getName(),
                p.getListPrice(),
                p.getDiscountRate(),
                p.getProductImageUrl(),
                p.getProductUrl(),
                isFavorited
        );
    }

    // 기존 코드 호환용(하트 모르면 false 기본)
    public static HomeProductItemResponse from(Product p) {
        return from(p, false);
    }
}

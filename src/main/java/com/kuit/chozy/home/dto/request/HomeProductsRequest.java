package com.kuit.chozy.home.dto.request;

import com.kuit.chozy.home.entity.ProductCategory;
import com.kuit.chozy.home.entity.ProductSort;

public record HomeProductsRequest(
        ProductCategory category,
        String search,
        ProductSort sort,
        Integer minPrice,
        Integer maxPrice,
        Double minRating,
        Double maxRating,
        Integer page,
        Integer size
) {
    public HomeProductsRequest {
        boolean hasCategory = category != null;
        boolean hasSearch = search != null && !search.isBlank();

        if (hasCategory == hasSearch) {
            throw new IllegalArgumentException("category 또는 search 중 하나만 지정해야 합니다.");
        }

        if (sort == null) sort = ProductSort.RELEVANCE;
        if (page == null) page = 0;
        if (size == null) size = 20;

        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new IllegalArgumentException("minPrice는 maxPrice보다 클 수 없습니다.");
        }
        if (minRating != null && maxRating != null && minRating > maxRating) {
            throw new IllegalArgumentException("minRating은 maxRating보다 클 수 없습니다.");
        }
    }
}

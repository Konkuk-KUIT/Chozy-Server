package com.kuit.chozy.home.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record HomeRecommendProductsRequest(
        @Min(0) Integer page,
        @Min(1) @Max(50) Integer size
) {
    public int pageOrDefault() { return page == null ? 0 : page; }
    public int sizeOrDefault() { return size == null ? 20 : size; }
}

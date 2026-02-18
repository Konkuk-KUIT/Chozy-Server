package com.kuit.chozy.likes.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LikeListRequest(
        String search,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size
) {
    public int pageOrDefault() {
        return page == null ? 0 : page;
    }

    public int sizeOrDefault() {
        return size == null ? 20 : size;
    }
}

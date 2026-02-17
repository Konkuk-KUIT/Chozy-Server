package com.kuit.chozy.global.common.response;

import com.kuit.chozy.home.dto.response.HomeProductItemResponse;

import java.util.List;

public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        boolean hasNext
) {
}

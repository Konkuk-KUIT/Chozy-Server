package com.kuit.chozy.home.dto.response;

import com.kuit.chozy.global.common.response.PageResult;

import java.util.List;

public record HomeProductsResult(
        PageResult<HomeProductItemResponse> result
) {}

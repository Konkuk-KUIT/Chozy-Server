package com.kuit.chozy.likes.dto.response;

import com.kuit.chozy.global.common.response.PageResult;

public record LikeListResult(
        PageResult<LikeItemResponse> result
) {
}

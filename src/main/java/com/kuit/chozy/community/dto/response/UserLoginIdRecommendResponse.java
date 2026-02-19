package com.kuit.chozy.community.dto.response;

public record UserLoginIdRecommendResponse(
        Long userId,
        String loginId,
        String profileImageUrl
) {
}

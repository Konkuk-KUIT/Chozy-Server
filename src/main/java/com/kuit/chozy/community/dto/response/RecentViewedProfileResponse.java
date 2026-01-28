package com.kuit.chozy.community.dto.response;

public record RecentViewedProfileResponse(
        Long profileId,
        String nickname,
        String profileImageUrl
) {
}

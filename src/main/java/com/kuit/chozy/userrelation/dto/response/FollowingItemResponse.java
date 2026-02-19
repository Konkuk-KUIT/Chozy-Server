package com.kuit.chozy.userrelation.dto.response;

import com.kuit.chozy.userrelation.dto.FollowStatus;

import java.time.LocalDateTime;

public record FollowingItemResponse(
        Long userId,
        String loginId,
        String nickname,
        String profileImageUrl,
        boolean isAccountPublic,
        FollowStatus myFollowStatus,
        boolean isFollowingByMe,
        boolean isFollowingMe,
        boolean isBlocked,
        boolean isCloseFriend,
        LocalDateTime followedAt
) {
}
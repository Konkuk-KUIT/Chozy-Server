package com.kuit.chozy.userrelation.dto.response;

import com.kuit.chozy.userrelation.dto.FollowStatus;

import java.time.LocalDateTime;

public class FollowerItemResponse {

    private final Long userId;
    private final String loginId;
    private final String nickname;
    private final String profileImageUrl;
    private final boolean isAccountPublic;
    private final boolean isFollowing;
    private final FollowStatus myFollowStatus;
    private final boolean isBlocked;
    private final boolean isCloseFriend;
    private final LocalDateTime followedAt;

    public FollowerItemResponse(
            Long userId,
            String loginId,
            String nickname,
            String profileImageUrl,
            boolean isAccountPublic,
            boolean isFollowing,
            FollowStatus myFollowStatus,
            boolean isBlocked,
            boolean isCloseFriend,
            LocalDateTime followedAt
    ) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.isAccountPublic = isAccountPublic;
        this.isFollowing = isFollowing;
        this.myFollowStatus = myFollowStatus;
        this.isBlocked = isBlocked;
        this.isCloseFriend = isCloseFriend;
        this.followedAt = followedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public boolean isAccountPublic() {
        return isAccountPublic;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public FollowStatus getMyFollowStatus() {
        return myFollowStatus;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public boolean isCloseFriend() {
        return isCloseFriend;
    }

    public LocalDateTime getFollowedAt() {
        return followedAt;
    }
}

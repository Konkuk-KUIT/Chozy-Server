package com.kuit.chozy.userrelation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kuit.chozy.userrelation.dto.FollowStatus;

import java.time.LocalDateTime;

public class FollowingItemResponse {

    private final Long userId;
    private final String loginId;
    private final String nickname;
    private final String profileImageUrl;

    @JsonProperty("isAccountPublic")
    private final boolean isAccountPublic;

    private final FollowStatus myFollowStatus;

    @JsonProperty("isFollowedByMe")
    private final boolean isFollowedByMe;

    @JsonProperty("isFollowingMe")
    private final boolean isFollowingMe;

    @JsonProperty("isBlocked")
    private final boolean isBlocked;

    @JsonProperty("isCloseFriend")
    private final boolean isCloseFriend;

    private final LocalDateTime followedAt;

    public FollowingItemResponse(
            Long userId,
            String loginId,
            String nickname,
            String profileImageUrl,
            boolean isAccountPublic,
            FollowStatus myFollowStatus,
            boolean isFollowedByMe,
            boolean isFollowingMe,
            boolean isBlocked,
            boolean isCloseFriend,
            LocalDateTime followedAt
    ) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.isAccountPublic = isAccountPublic;
        this.myFollowStatus = myFollowStatus;
        this.isFollowedByMe = isFollowedByMe;
        this.isFollowingMe = isFollowingMe;
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

    @JsonProperty("isAccountPublic")
    public boolean isAccountPublic() {
        return isAccountPublic;
    }

    public FollowStatus getMyFollowStatus() {
        return myFollowStatus;
    }

    @JsonProperty("isFollowedByMe")
    public boolean isFollowedByMe() {
        return isFollowedByMe;
    }

    @JsonProperty("isFollowingMe")
    public boolean isFollowingMe() {
        return isFollowingMe;
    }

    @JsonProperty("isBlocked")
    public boolean isBlocked() {
        return isBlocked;
    }

    @JsonProperty("isCloseFriend")
    public boolean isCloseFriend() {
        return isCloseFriend;
    }

    public LocalDateTime getFollowedAt() {
        return followedAt;
    }
}
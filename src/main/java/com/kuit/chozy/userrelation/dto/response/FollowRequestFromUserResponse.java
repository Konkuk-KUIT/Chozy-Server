package com.kuit.chozy.userrelation.dto.response;

public class FollowRequestFromUserResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String profileImageUrl;

    public FollowRequestFromUserResponse(Long userId, String loginId, String nickname, String profileImageUrl) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
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
}

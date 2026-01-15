package com.kuit.chozy.userrelation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CloseFriendItemResponse {

    private Long userId;
    private String loginId;
    private String nickname;
    private String profileImageUrl;
    private boolean isAccountPublic;
    private LocalDateTime setAt;

    public CloseFriendItemResponse(
            Long userId,
            String loginId,
            String nickname,
            String profileImageUrl,
            boolean isAccountPublic,
            LocalDateTime setAt
    ) {
        this.userId = userId;
        this.loginId = loginId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.isAccountPublic = isAccountPublic;
        this.setAt = setAt;
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

    public LocalDateTime getSetAt() {
        return setAt;
    }
}

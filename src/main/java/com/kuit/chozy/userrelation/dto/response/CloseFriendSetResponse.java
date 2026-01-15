package com.kuit.chozy.userrelation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CloseFriendSetResponse {

    private Long targetUserId;
    private boolean isCloseFriend;
    private LocalDateTime setAt;

    public CloseFriendSetResponse(Long targetUserId, boolean isCloseFriend, LocalDateTime setAt) {
        this.targetUserId = targetUserId;
        this.isCloseFriend = isCloseFriend;
        this.setAt = setAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    @JsonProperty("isCloseFriend")
    public boolean isCloseFriend() {
        return isCloseFriend;
    }

    public LocalDateTime getSetAt() {
        return setAt;
    }
}
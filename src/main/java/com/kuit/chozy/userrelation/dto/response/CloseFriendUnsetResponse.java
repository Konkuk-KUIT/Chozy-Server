package com.kuit.chozy.userrelation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CloseFriendUnsetResponse {

    private Long targetUserId;
    private boolean isCloseFriend;
    private LocalDateTime unsetAt;

    public CloseFriendUnsetResponse(Long targetUserId, boolean isCloseFriend, LocalDateTime unsetAt) {
        this.targetUserId = targetUserId;
        this.isCloseFriend = isCloseFriend;
        this.unsetAt = unsetAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    @JsonProperty("isCloseFriend")
    public boolean isCloseFriend() {
        return isCloseFriend;
    }

    public LocalDateTime getUnsetAt() {
        return unsetAt;
    }
}

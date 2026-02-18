package com.kuit.chozy.userrelation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class UnmuteResponse {

    private final Long targetUserId;
    private final boolean isMuted;
    private final LocalDateTime unmutedAt;

    public UnmuteResponse(Long targetUserId, boolean isMuted, LocalDateTime unmutedAt) {
        this.targetUserId = targetUserId;
        this.isMuted = isMuted;
        this.unmutedAt = unmutedAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    @JsonProperty("isMuted")
    public boolean isMuted() {
        return isMuted;
    }

    public LocalDateTime getUnmutedAt() {
        return unmutedAt;
    }
}

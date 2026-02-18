package com.kuit.chozy.userrelation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MuteResponse {

    private final Long targetUserId;
    private final boolean isMuted;
    private final LocalDateTime mutedAt;

    public MuteResponse(Long targetUserId, boolean isMuted, LocalDateTime mutedAt) {
        this.targetUserId = targetUserId;
        this.isMuted = isMuted;
        this.mutedAt = mutedAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    @JsonProperty("isMuted")
    public boolean isMuted() {
        return isMuted;
    }

    public LocalDateTime getMutedAt() {
        return mutedAt;
    }
}

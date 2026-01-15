package com.kuit.chozy.userrelation.dto.response;

import java.time.LocalDateTime;

public class BlockResponse {

    private final Long targetUserId;
    private final boolean isBlocked;
    private final LocalDateTime blockedAt;

    public BlockResponse(Long targetUserId, boolean isBlocked, LocalDateTime blockedAt) {
        this.targetUserId = targetUserId;
        this.isBlocked = isBlocked;
        this.blockedAt = blockedAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }
}
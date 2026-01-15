package com.kuit.chozy.userrelation.dto.response;

import java.time.LocalDateTime;

public class UnblockResponse {

    private final Long targetUserId;
    private final boolean isBlocked;
    private final LocalDateTime unblockedAt;

    public UnblockResponse(Long targetUserId, boolean isBlocked, LocalDateTime unblockedAt) {
        this.targetUserId = targetUserId;
        this.isBlocked = isBlocked;
        this.unblockedAt = unblockedAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public LocalDateTime getUnblockedAt() {
        return unblockedAt;
    }
}

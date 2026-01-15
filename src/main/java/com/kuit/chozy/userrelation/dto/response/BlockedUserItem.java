package com.kuit.chozy.userrelation.dto.response;

import java.time.LocalDateTime;

public class BlockedUserItem {

    private final Long targetUserId;
    private final LocalDateTime blockedAt;

    public BlockedUserItem(Long targetUserId, LocalDateTime blockedAt) {
        this.targetUserId = targetUserId;
        this.blockedAt = blockedAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }
}

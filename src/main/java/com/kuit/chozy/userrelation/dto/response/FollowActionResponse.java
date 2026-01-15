package com.kuit.chozy.userrelation.dto.response;

import com.kuit.chozy.userrelation.dto.FollowStatus;

import java.time.LocalDateTime;

public class FollowActionResponse {

    private final Long targetUserId;
    private final FollowStatus followStatus;
    private final Long requestId;
    private final LocalDateTime requestedAt;

    public FollowActionResponse(Long targetUserId, FollowStatus followStatus, Long requestId, LocalDateTime requestedAt) {
        this.targetUserId = targetUserId;
        this.followStatus = followStatus;
        this.requestId = requestId;
        this.requestedAt = requestedAt;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public FollowStatus getFollowStatus() {
        return followStatus;
    }

    public Long getRequestId() {
        return requestId;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}

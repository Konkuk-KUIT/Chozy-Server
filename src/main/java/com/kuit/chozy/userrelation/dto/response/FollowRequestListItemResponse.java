package com.kuit.chozy.userrelation.dto.response;

import java.time.LocalDateTime;

public class FollowRequestListItemResponse {

    private Long requestId;
    private FollowRequestFromUserResponse fromUser;
    private LocalDateTime requestedAt;

    public FollowRequestListItemResponse(Long requestId, FollowRequestFromUserResponse fromUser, LocalDateTime requestedAt) {
        this.requestId = requestId;
        this.fromUser = fromUser;
        this.requestedAt = requestedAt;
    }

    public Long getRequestId() {
        return requestId;
    }

    public FollowRequestFromUserResponse getFromUser() {
        return fromUser;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}

package com.kuit.chozy.userrelation.dto.response;

import java.time.LocalDateTime;

public class FollowRequestProcessResponse {

    private Long requestId;
    private String status; // "ACCEPTED" | "REJECTED"
    private LocalDateTime processedAt;
    private Long fromUserId;
    private Long toUserId;

    public FollowRequestProcessResponse(
            Long requestId,
            String status,
            LocalDateTime processedAt,
            Long fromUserId,
            Long toUserId
    ) {
        this.requestId = requestId;
        this.status = status;
        this.processedAt = processedAt;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }

    public Long getRequestId() {
        return requestId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public static FollowRequestProcessResponse accepted(
            Long requestId,
            LocalDateTime processedAt,
            Long fromUserId,
            Long toUserId
    ) {
        return new FollowRequestProcessResponse(
                requestId,
                "ACCEPTED",
                processedAt,
                fromUserId,
                toUserId
        );
    }

    public static FollowRequestProcessResponse rejected(
            Long requestId,
            LocalDateTime processedAt,
            Long fromUserId,
            Long toUserId
    ) {
        return new FollowRequestProcessResponse(
                requestId,
                "REJECTED",
                processedAt,
                fromUserId,
                toUserId
        );
    }
}

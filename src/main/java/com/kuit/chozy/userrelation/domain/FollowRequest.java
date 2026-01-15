package com.kuit.chozy.userrelation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "follow_requests")
public class FollowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FollowRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected FollowRequest() {
    }

    public FollowRequest(Long requesterId, Long targetId, FollowRequestStatus status, LocalDateTime requestedAt) {
        this.requesterId = requesterId;
        this.targetId = targetId;
        this.status = status;
        this.requestedAt = requestedAt;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public FollowRequestStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void changeStatus(FollowRequestStatus status) {
        this.status = status;
    }

    public boolean isPending() {
        return this.status == FollowRequestStatus.PENDING;
    }

    public void accept(LocalDateTime processedAt) {
        this.status = FollowRequestStatus.ACCEPTED;
        this.processedAt = processedAt;
    }

    public void reject(LocalDateTime processedAt) {
        this.status = FollowRequestStatus.REJECTED;
        this.processedAt = processedAt;
    }

}
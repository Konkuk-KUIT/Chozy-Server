package com.kuit.chozy.userrelation.domain;

import com.kuit.chozy.userrelation.dto.FollowStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_follows_follower_following",
                        columnNames = {"follower_id", "following_id"}
                )
        }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    @Column(name = "following_id", nullable = false)
    private Long followingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FollowStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Follow() {
    }

    public Follow(Long followerId, Long followingId, LocalDateTime createdAt) {
        this.followerId = followerId;
        this.followingId = followingId;
        this.status = FollowStatus.FOLLOWING; // 기본값
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public Long getFollowingId() {
        return followingId;
    }

    public FollowStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateStatus(FollowStatus status) {
        this.status = status;
    }
}

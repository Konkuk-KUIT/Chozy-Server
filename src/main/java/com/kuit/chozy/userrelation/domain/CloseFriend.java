package com.kuit.chozy.userrelation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "close_friend",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_close_friend_user_target", columnNames = {"user_id", "target_user_id"})
        }
)
public class CloseFriend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;

    @Column(name = "set_at", nullable = false)
    private LocalDateTime setAt;

    protected CloseFriend() {
    }

    public CloseFriend(Long userId, Long targetUserId, LocalDateTime setAt) {
        this.userId = userId;
        this.targetUserId = targetUserId;
        this.setAt = setAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public LocalDateTime getSetAt() {
        return setAt;
    }
}

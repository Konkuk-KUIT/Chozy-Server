package com.kuit.chozy.userrelation.domain;

import com.kuit.chozy.auth.entity.TokenStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "blocks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_blocks_blocker_blocked", columnNames = {"blocker_id", "blocked_id"})
        },
        indexes = {
                @Index(name = "idx_blocks_blocker_active", columnList = "blocker_id, active")
        }
)
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;

    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;

    @Column(nullable = false)
    private boolean active;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Block() {
    }

    public Block(Long blockerId, Long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = TokenStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Long getBlockerId() {
        return blockerId;
    }

    public Long getBlockedId() {
        return blockedId;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getBlockedAt() {
        return createdAt;
    }

    public void activate() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }
}

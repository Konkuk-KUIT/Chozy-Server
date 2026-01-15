package com.kuit.chozy.userrelation.domain;

import jakarta.persistence.*;
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

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    protected Block() {
    }

    public Block(Long blockerId, Long blockedId) {
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.active = true;
        this.blockedAt = LocalDateTime.now();
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
        return blockedAt;
    }

    public void activate() {
        this.active = true;
        this.blockedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }
}

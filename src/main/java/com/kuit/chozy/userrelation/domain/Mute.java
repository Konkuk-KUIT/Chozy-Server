package com.kuit.chozy.userrelation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mutes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mutes_muter_muted", columnNames = {"muter_id", "muted_id"})
        },
        indexes = {
                @Index(name = "idx_mutes_muter_active", columnList = "muter_id, active")
        }
)
public class Mute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "muter_id", nullable = false)
    private Long muterId;

    @Column(name = "muted_id", nullable = false)
    private Long mutedId;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "muted_at", nullable = false)
    private LocalDateTime mutedAt;

    protected Mute() {
    }

    public Mute(Long muterId, Long mutedId) {
        this.muterId = muterId;
        this.mutedId = mutedId;
        this.active = true;
        this.mutedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMuterId() {
        return muterId;
    }

    public Long getMutedId() {
        return mutedId;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getMutedAt() {
        return mutedAt;
    }

    public void activate() {
        this.active = true;
        this.mutedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }
}

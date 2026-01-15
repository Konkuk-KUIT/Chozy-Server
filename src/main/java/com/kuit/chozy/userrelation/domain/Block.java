package com.kuit.chozy.userrelation.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "blocks")
public class Block {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blocker_id", nullable = false)
    private Long blockerId;

    @Column(name = "blocked_id", nullable = false)
    private Long blockedId;

    protected Block() {
    }

    public Long getBlockerId() {
        return blockerId;
    }

    public Long getBlockedId() {
        return blockedId;
    }
}

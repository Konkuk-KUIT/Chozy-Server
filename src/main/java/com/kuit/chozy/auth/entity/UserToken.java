package com.kuit.chozy.auth.entity;

import com.kuit.chozy.auth.entity.TokenStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_token")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refresh_token", nullable = false, length = 512)
    private String refreshToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private TokenStatus status;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserToken(Long userId, String refreshToken) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.status = TokenStatus.ACTIVE;
    }

    public static UserToken create(Long userId, String refreshToken) {
        return new UserToken(userId, refreshToken);
    }

    public void inactivate() {
        this.status = TokenStatus.INACTIVE;
    }

    public void delete() {
        this.status = TokenStatus.DELETED;
    }
}

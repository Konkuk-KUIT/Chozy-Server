package com.kuit.chozy.auth.entity;

import com.kuit.chozy.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_auth",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_auth_provider_provider_user_id",
                        columnNames = {"provider", "provider_user_id"}
                ),
                @UniqueConstraint(
                        name = "uk_user_auth_login_id",
                        columnNames = {"login_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* 한 유저가 여러 인증수단(LOCAL/KAKAO)을 가질 수 있음 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", length = 100)
    private String providerUserId;

    @Column(name = "login_id", length = 50)
    private String loginId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static UserAuth createLocal(User user, String loginId, String passwordHash) {
        return UserAuth.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .loginId(loginId)
                .passwordHash(passwordHash)
                .build();
    }

    public static UserAuth createKakao(User user, String providerUserId) {
        return UserAuth.builder()
                .user(user)
                .provider(AuthProvider.KAKAO)
                .providerUserId(providerUserId)
                .build();
    }

    public static UserAuth createNaver(User user, String providerUserId) {
        return UserAuth.builder()
                .user(user)
                .provider(AuthProvider.NAVER)
                .providerUserId(providerUserId)
                .build();
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

}
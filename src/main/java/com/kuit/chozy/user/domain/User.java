package com.kuit.chozy.user.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_account_public", nullable = false)
    private boolean isAccountPublic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    protected User() {
    }

    public Long getId() {
        return id;
    }

    // 공개 계정이면 true
    public boolean isAccountPublic() {
        return isAccountPublic;
    }

    public UserStatus getStatus() {
        return status;
    }

    // 팔로우에서 쓸 활성 여부 헬퍼
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}

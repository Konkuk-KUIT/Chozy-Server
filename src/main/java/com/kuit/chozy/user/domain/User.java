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

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

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

    public String getLoginId() {
        return loginId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }


    // 팔로우에서 쓸 활성 여부 헬퍼
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }
}

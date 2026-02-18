package com.kuit.chozy.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =============== Auth ===============
    @Column(unique = true, length = 50)
    private String loginId;

    @Column
    private String password;

    @Column(unique = true)
    private String email;

    // =============== Profile ===============
    @Column
    private String name;

    @Column(length = 50)
    private String nickname;

    @Column(length = 20)
    private String phoneNumber;

    @Column
    private String statusMessage;

    @Column
    private String country;

    @Column
    private LocalDate birthDate;

    @Column
    private Float height; // cm

    @Column
    private Float weight; // kg

    @Column(length = 2048)
    private String profileImageUrl;

    @Column(length = 2048)
    private String backgroundImageUrl;

    // =============== Privacy ===============
    @Column(name = "is_account_public")
    private Boolean isAccountPublic;

    @Column(name = "is_birth_public")
    private Boolean isBirthPublic;

    @Column(name = "is_height_public")
    private Boolean isHeightPublic;

    @Column(name = "is_weight_public")
    private Boolean isWeightPublic;

    // =============== Status ===============
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @Column(name = "password_updated_at")
    private LocalDateTime passwordUpdatedAt;

    // 팔로우에서 쓸 활성 여부 헬퍼
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void delete() {
        this.status = UserStatus.DELETED;
    }
}

package com.kuit.chozy.community.entity;

import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recent_viewed_profiles",
        indexes = {
                @Index(name = "idx_recent_viewed_viewer", columnList = "viewer_id"),
                @Index(name = "idx_recent_viewed_visited", columnList = "visited_user_id"),
                @Index(name = "idx_recent_viewed_updated", columnList = "viewer_id, updated_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_viewer_visited",
                        columnNames = {"viewer_id", "visited_user_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentViewedProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 방문한 사람 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer;

    /** 방문당한 사람 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visited_user_id", nullable = false)
    private User visitedUser;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    /** 생성 시간 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 최근 방문 시간 (갱신됨) */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ====== 생성용 팩토리 ====== */
    public static RecentViewedProfile create(User viewer, User visitedUser) {
        RecentViewedProfile profile = new RecentViewedProfile();
        profile.viewer = viewer;
        profile.visitedUser = visitedUser;
        profile.status = UserStatus.ACTIVE;
        return profile;
    }

    /* ====== 재방문 시 ====== */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
}

package com.kuit.chozy.community.domain;

import com.kuit.chozy.home.entity.SearchStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "community_search_histories",
        indexes = {
                @Index(name = "idx_search_histories_user", columnList = "user_id"),
                @Index(name = "idx_search_histories_keyword", columnList = "keyword")
        }
)
public class CommunitySearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 ID (User 엔티티가 있다면 ManyToOne으로 변경 가능) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 검색 키워드 */
    @Column(nullable = false, length = 100)
    private String keyword;

    /** 특정 기간 내 검색 횟수 */
    @Column(name = "count_in_period", nullable = false)
    private int countInPeriod;

    /** 누적 검색 횟수 */
    @Column(name = "count_total", nullable = false)
    private int countTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /* ===== 생성 메서드 ===== */
    public static CommunitySearchHistory create(Long userId, String keyword) {
        CommunitySearchHistory history = new CommunitySearchHistory();
        history.userId = userId;
        history.keyword = keyword;
        history.countInPeriod = 1;
        history.countTotal = 1;
        history.status = SearchStatus.ACTIVE;
        return history;
    }

    /* ===== 도메인 메서드 ===== */
    public void increaseCount() {
        this.countInPeriod++;
        this.countTotal++;
    }

    public void resetPeriodCount() {
        this.countInPeriod = 0;
    }

    public void deactivate() {
        this.status = SearchStatus.INACTIVE;
    }

    public void delete() {
        this.status = SearchStatus.DELETED;
    }
}

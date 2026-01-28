package com.kuit.chozy.home.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "home_trending_count",
        indexes = {
                @Index(name = "idx_trending_keyword", columnList = "keyword")
        }
)
public class TrendingCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 검색 키워드 */
    @Column(nullable = false, length = 100)
    private String keyword;

    /** 하루 카운트 */
    @Column(name = "daily_count", nullable = false)
    private int dailyCount;

    /** 전날 순위 */
    @Column(name = "rank_yesterday", nullable = false)
    private int rankYesterday;

    /** 오늘 순위 */
    @Column(name = "rank_today", nullable = false)
    private int rankToday;

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
    public static TrendingCount create(String keyword, int rankToday) {
        TrendingCount trending = new TrendingCount();
        trending.keyword = keyword;
        trending.dailyCount = 0;
        trending.rankToday = 0;
        trending.rankYesterday = 0;
        trending.status = SearchStatus.ACTIVE;
        return trending;
    }

    /* ===== 도메인 메서드 ===== */
    public void updateRank(int newRank) {
        this.rankYesterday = this.rankToday;
        this.rankToday = newRank;
    }

    public int getRankDiff() {
        return this.rankYesterday - this.rankToday;
    }

    public void delete() {
        this.status = SearchStatus.DELETED;
    }

    public void moveTodayToYesterday() {
        this.rankYesterday = this.rankToday;
    }

    public void setTodayRank(int rank) {
        this.rankToday = rank;
    }

    public void resetDailyCount() {
        this.dailyCount = 0;
    }

    public void increaseDailyCount() {
        this.dailyCount++;
    }

}

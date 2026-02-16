package com.kuit.chozy.community.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "feed",
        indexes = {
                @Index(name = "idx_feed_user_id", columnList = "user_id"),
                @Index(name = "idx_feed_content_type", columnList = "content_type"),
                @Index(name = "idx_feed_created_at", columnList = "created_at")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private FeedKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private FeedContentType contentType;

    @Column(name = "original_feed_id")
    private Long originalFeedId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "quote_text", length = 500)
    private String quoteText;

    @Column(name = "hashtags", nullable = false, columnDefinition = "json")
    @Builder.Default
    private String hashtags = "[]";

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "dislike_count", nullable = false)
    @Builder.Default
    private Integer dislikeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "share_count", nullable = false)
    @Builder.Default
    private Integer shareCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private FeedStatus status = FeedStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // REVIEW 타입 전용
    @Column(name = "vendor")
    private String vendor;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "rating", precision = 2, scale = 1)
    private java.math.BigDecimal rating;

    @Column(name = "product_url", length = 2048)
    private String productUrl;
}
package com.kuit.chozy.community.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "feeds",
        indexes = {
                @Index(name = "idx_feeds_user_id", columnList = "user_id"),
                @Index(name = "idx_feeds_content_type", columnList = "content_type"),
                @Index(name = "idx_feeds_created_at", columnList = "created_at")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private FeedContentType contentType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 5000)
    private String text;

    @ElementCollection
    @CollectionTable(name = "feed_images", joinColumns = @JoinColumn(name = "feed_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> contentImgs = new ArrayList<>();

    // REVIEW 타입 전용 (POST 타입은 null)
    @Column(length = 200)
    private String vendor;

    @Column(name = "product_url", length = 2048)
    private String productUrl;

    @Column(length = 500)
    private String title;

    private Float rating;

    @Column(name = "quote_feed_id")
    private Long quoteFeedId;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "dislike_count", nullable = false)
    @Builder.Default
    private Long dislikeCount = 0L;

    @Column(name = "quote_count", nullable = false)
    @Builder.Default
    private Long quoteCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

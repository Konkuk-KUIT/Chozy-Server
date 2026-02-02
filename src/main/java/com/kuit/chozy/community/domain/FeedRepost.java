package com.kuit.chozy.community.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "feed_reposts",
        indexes = {
                @Index(name = "idx_feed_reposts_user_id", columnList = "user_id"),
                @Index(name = "idx_feed_reposts_source_feed_id", columnList = "source_feed_id"),
                @Index(name = "idx_feed_reposts_target_feed_id", columnList = "target_feed_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedRepost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_feed_id", nullable = false)
    private Long sourceFeedId;

    @Column(name = "target_feed_id", nullable = false)
    private Long targetFeedId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

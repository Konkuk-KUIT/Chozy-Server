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
        name = "feed_bookmarks",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_bookmarks_user_feed",
                        columnNames = {"user_id", "feed_id"}
                )
        },
        indexes = {
                @Index(name = "idx_feed_bookmarks_user_id", columnList = "user_id"),
                @Index(name = "idx_feed_bookmarks_feed_id", columnList = "feed_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedBookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "feed_id", nullable = false)
    private Long feedId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

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
        name = "feed_comment_reactions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_feed_comment_reactions_user_comment",
                        columnNames = {"user_id", "comment_id"}
                )
        },
        indexes = {
                @Index(name = "idx_feed_comment_reactions_user_id", columnList = "user_id"),
                @Index(name = "idx_feed_comment_reactions_comment_id", columnList = "comment_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedCommentReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 20)
    private ReactionType reactionType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}

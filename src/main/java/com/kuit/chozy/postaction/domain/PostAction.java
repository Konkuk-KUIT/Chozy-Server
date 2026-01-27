package com.kuit.chozy.postaction.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_actions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_actions_user_post_type",
                        columnNames = {"user_id", "post_id", "type"}
                )
        }
)
public class PostAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostActionType type;

    @Column(name = "hashtags", nullable = false, columnDefinition = "json")
    private String hashTags;

    @Column(name = "quote_text", length = 500)
    private String quoteText;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostActionStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PostAction() {
    }

    private PostAction(PostActionType type, String hashTags, String quoteText, Long postId, Long userId) {
        this.type = type;
        this.hashTags = hashTags;
        this.quoteText = quoteText;
        this.postId = postId;
        this.userId = userId;
        this.status = PostActionStatus.ACTIVE;
    }

    public static PostAction retweet(Long postId, Long userId, String hashTagsJson) {
        return new PostAction(PostActionType.REPOST, hashTagsJson, null, postId, userId);
    }

    public Long getId() {
        return id;
    }

    public PostActionType getType() {
        return type;
    }

    public String getHashTags() {
        return hashTags;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getUserId() {
        return userId;
    }

    public PostActionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void delete() {
        this.status = PostActionStatus.DELETED;
    }
}
package com.kuit.chozy.review.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "product_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_review_product_user", columnNames = {"product_id", "user_id"})
        }
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_link", length = 2048)
    private String sourceLink;

    @Column(name = "rating", nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;

    @Lob
    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReviewStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Review() {
    }

    public Review(Long productId, Long userId, String sourceLink, BigDecimal rating, String content) {
        this.productId = productId;
        this.userId = userId;
        this.sourceLink = sourceLink;
        this.rating = rating;
        this.content = content;
        this.status = ReviewStatus.ACTIVE;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ReviewStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getSourceLink() {
        return sourceLink;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void validateUpdatable(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalStateException("UNAUTHORIZED");
        }
        if (!this.userId.equals(userId)) {
            throw new IllegalStateException("FORBIDDEN");
        }
        if (this.status == ReviewStatus.DELETED) {
            throw new IllegalStateException("DELETED");
        }
    }

    public void update(String sourceLink, BigDecimal rating, String content) {
        if (sourceLink != null) {
            this.sourceLink = sourceLink;
        }
        if (rating != null) {
            this.rating = rating;
        }
        if (content != null) {
            this.content = content;
        }
    }
}
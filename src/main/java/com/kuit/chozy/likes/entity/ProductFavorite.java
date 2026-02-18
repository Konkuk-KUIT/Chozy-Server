package com.kuit.chozy.likes.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_favorites")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(ProductFavorite.PK.class)
public class ProductFavorite {

    @Id
    @Column(name="user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name="product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FavoriteStatus status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 멱등 처리용 */
    public void activate(LocalDateTime now) {
        if (this.createdAt == null) this.createdAt = now;
        this.status = FavoriteStatus.ACTIVE;
        this.updatedAt = now;
    }

    public void deactivate(LocalDateTime now) {
        if (this.createdAt == null) this.createdAt = now;
        this.status = FavoriteStatus.INACTIVE;
        this.updatedAt = now;
    }

    public void delete(LocalDateTime now) {
        if (this.createdAt == null) this.createdAt = now;
        this.status = FavoriteStatus.DELETED;
        this.updatedAt = now;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PK implements Serializable {
        private Long userId;
        private Long productId;
    }
}

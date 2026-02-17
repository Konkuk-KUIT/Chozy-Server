package com.kuit.chozy.home.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recent_viewed_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecentViewedProduct {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="is_favorited_snapshot", nullable = false)
    private Boolean isFavoritedSnapshot;

    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="product_id", nullable = false)
    private Long productId;

    @Column(name="viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}

package com.kuit.chozy.home.entity;// Product.java
import com.kuit.chozy.home.external.ProductSnapshot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_vendor_external_product_id",
                columnNames = {"vendor", "external_product_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Vendor vendor;

    @Column(name = "external_product_id", nullable = false)
    private Long externalProductId; // 쿠팡 productId / 알리 id

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "list_price", nullable = false)
    private Integer listPrice;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    @Column(name = "discounted_price", nullable = false)
    private Integer discountedPrice;

    @Column(name = "product_image_url", length = 2048)
    private String productImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategory category;

    @Column(name = "product_url", nullable = false, length = 2048)
    private String productUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name="created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void applySnapshot(ProductSnapshot s) {
        // 외부키는 바뀌면 안됨
        this.name = s.getName();
        this.listPrice = s.getListPrice();
        this.discountRate = s.getDiscountRate() == null ? 0 : s.getDiscountRate();
        this.productImageUrl = s.getProductImageUrl();
        this.category = s.getCategory();
        this.discountedPrice = s.getDiscountedPrice() == null ? 0 : s.getDiscountedPrice();
        this.productUrl = s.getProductUrl();
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
}

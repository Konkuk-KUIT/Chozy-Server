package com.kuit.chozy.home.external;
import com.kuit.chozy.home.entity.ProductCategory;
import com.kuit.chozy.home.entity.Vendor;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductSnapshot {
    private Vendor vendor;
    private Long externalProductId;
    private ProductCategory category;

    private String name;
    private Integer listPrice;
    private Integer discountRate;
    private Integer discountedPrice;
    private String productImageUrl;
    private String productUrl;

    private Double ratingAvg;

    // 쿠팡
    private String impressionUrl;
    private Boolean isRocket;
}

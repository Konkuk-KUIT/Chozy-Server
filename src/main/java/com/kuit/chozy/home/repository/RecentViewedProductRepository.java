package com.kuit.chozy.home.repository;

import com.kuit.chozy.home.entity.ProductStatus;
import com.kuit.chozy.home.entity.RecentViewedProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentViewedProductRepository extends JpaRepository<RecentViewedProduct, Long> {
    Optional<RecentViewedProduct> findByUserIdAndProductIdAndStatus(Long userId, Long productId, ProductStatus status);

    List<RecentViewedProduct> findByUserIdAndStatusOrderByViewedAtDesc(
            Long userId,
            ProductStatus status,
            Pageable pageable
    );
}

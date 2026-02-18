package com.kuit.chozy.likes.repository;

import com.kuit.chozy.likes.entity.FavoriteStatus;
import com.kuit.chozy.likes.entity.ProductFavorite;
import com.kuit.chozy.home.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, ProductFavorite.PK> {
    Optional<ProductFavorite> findByUserIdAndProductId(Long userId, Long productId);

    Page<ProductFavorite> findByUserIdAndStatus(Long userId, FavoriteStatus status, Pageable pageable);

    List<ProductFavorite> findByUserIdAndProductIdInAndStatus(Long userId, List<Long> productIds, FavoriteStatus status);
}

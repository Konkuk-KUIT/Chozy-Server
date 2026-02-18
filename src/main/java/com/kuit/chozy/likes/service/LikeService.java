package com.kuit.chozy.likes.service;

import com.kuit.chozy.global.common.response.PageResult;
import com.kuit.chozy.home.entity.Product;
import com.kuit.chozy.home.entity.ProductStatus;
import com.kuit.chozy.home.repository.ProductRepository;
import com.kuit.chozy.likes.dto.response.LikeItemResponse;
import com.kuit.chozy.likes.dto.response.LikeListResult;
import com.kuit.chozy.likes.entity.FavoriteStatus;
import com.kuit.chozy.likes.entity.ProductFavorite;
import com.kuit.chozy.likes.repository.ProductFavoriteRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final ProductFavoriteRepository productFavoriteRepository;
    private final ProductRepository productRepository;

    public String likeProduct(Long userId, @NotNull Long productId, @NotNull Boolean like) {
        LocalDateTime now = LocalDateTime.now();

        ProductFavorite favorite = productFavoriteRepository
                .findByUserIdAndProductId(userId, productId)
                .orElseGet(() -> ProductFavorite.builder()
                        .userId(userId)
                        .productId(productId)
                        .status(FavoriteStatus.INACTIVE)
                        .createdAt(now)
                        .updatedAt(now)
                        .build());

        if (like) {
            favorite.activate(now);
            productFavoriteRepository.save(favorite);
            return "상품을 찜했어요.";
        } else {
            favorite.deactivate(now);
            productFavoriteRepository.save(favorite);
            return "찜을 해제했어요.";
        }
    }

    @Transactional
    public LikeListResult getLikeProducts(Long userId, String search, int page, int size) {
        Page<Product> likedPage = productRepository.findLikedProducts(
                userId,
                FavoriteStatus.ACTIVE,
                ProductStatus.ACTIVE,
                search,
                PageRequest.of(page, size)
        );

        List<LikeItemResponse> items = likedPage.getContent().stream()
                .map(LikeItemResponse::from)
                .toList();

        PageResult<LikeItemResponse> pageResult =
                new PageResult<>(
                        items,
                        likedPage.getNumber(),
                        likedPage.getSize(),
                        likedPage.hasNext()
                );

        return new LikeListResult(pageResult);
    }
}

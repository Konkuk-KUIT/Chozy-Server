package com.kuit.chozy.home.service;

import com.kuit.chozy.global.common.response.PageResult;
import com.kuit.chozy.home.dto.request.HomeProductsRequest;
import com.kuit.chozy.home.dto.response.HomeProductItemResponse;
import com.kuit.chozy.home.dto.response.HomeProductsResult;
import com.kuit.chozy.home.entity.*;
import com.kuit.chozy.home.external.ExternalProductClient;
import com.kuit.chozy.home.external.ProductSnapshot;
import com.kuit.chozy.likes.entity.FavoriteStatus;
import com.kuit.chozy.likes.entity.ProductFavorite;
import com.kuit.chozy.likes.repository.ProductFavoriteRepository;
import com.kuit.chozy.home.repository.ProductRepository;
import com.kuit.chozy.home.repository.RecentViewedProductRepository;
import com.kuit.chozy.home.repository.TrendingCountRepository;
import com.kuit.chozy.home.service.spec.ProductSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeProductService {

    private static final int EXTERNAL_FETCH_MAX_SIZE = 100;  // 쿠팡 API limit 상한
    private static final int FIXED_SIZE = 6; // 최근 본 상품 조회 개수
    private static final int RECENT_CATEGORY_SOURCE_MAX = 500; // 상품 추천 개수 상한선
    private static final int TRENDING_KEYWORD_MAX = 20;

    private final ProductRepository productRepository;
    private final RecentViewedProductRepository recentViewedProductRepository;
    private final ProductFavoriteRepository productFavoriteRepository;
    private final TrendingCountRepository trendingCountRepository;
    private final List<ExternalProductClient> externalClients;


    @Transactional
    public HomeProductsResult getHomeProducts(Long userId, HomeProductsRequest r) {

        Page<Product> page = queryDb(r);

        int requiredCount = (r.page() + 1) * r.size();

        if (page.getTotalElements() < requiredCount) {
            fetchFromExternalAndUpsert(r, requiredCount);
            page = queryDb(r);
        }

        return toUserProductPage(userId, page);
    }

    private Page<Product> queryDb(HomeProductsRequest r) {
        Specification<Product> spec = Specification.where(ProductSpecs.isActive());

        // category / search 중 하나만 온다는 전제 (request에서 검증)
        if (r.category() != null) spec = spec.and(ProductSpecs.categoryEquals(r.category()));
        if (r.search() != null && !r.search().isBlank()) spec = spec.and(ProductSpecs.nameContains(r.search()));

        if (r.minPrice() != null) spec = spec.and(ProductSpecs.priceGte(r.minPrice()));
        if (r.maxPrice() != null) spec = spec.and(ProductSpecs.priceLte(r.maxPrice()));
        if (r.minRating() != null) spec = spec.and(ProductSpecs.ratingGte(r.minRating()));
        if (r.maxRating() != null) spec = spec.and(ProductSpecs.ratingLte(r.maxRating()));

        Pageable pageable = PageRequest.of(
                r.page(),
                r.size(),
                toSort(r.sort())
        );

        return productRepository.findAll(spec, pageable);
    }

    private Sort toSort(ProductSort sort) {
        return switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "listPrice");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "listPrice");
            case RELEVANCE -> Sort.by(Sort.Direction.DESC, "updatedAt");
        };
    }


    private void fetchFromExternalAndUpsert(HomeProductsRequest r, long requiredCount) {
        ExternalProductClient coupang = externalClients.stream()
                .filter(c -> c.supports() == Vendor.COUPANG)
                .findFirst()
                .orElse(null);

        if (coupang == null) return;

        // 현재 DB에 조건에 맞는 총 개수 다시 계산
        long currentTotal = countDb(r);
        if (currentTotal >= requiredCount) return;

        long remainingCount = requiredCount - currentTotal;

        int limit = (int) Math.min(EXTERNAL_FETCH_MAX_SIZE, currentTotal + remainingCount);

        // request의 size를 외부 호출에 맞게 줄여서 호출 (record니까 "size만 바꾼 새 request" 생성)
        HomeProductsRequest externalReq = new HomeProductsRequest(
                r.category(),
                r.search(),
                r.sort(),
                r.minPrice(),
                r.maxPrice(),
                r.minRating(),
                r.maxRating(),
                0,               // 외부는 page 의미 없음
                limit
        );

        List<ProductSnapshot> snaps = coupang.fetch(externalReq);
        if (snaps == null || snaps.isEmpty()) return;

        upsertAll(snaps);
    }

    private long countDb(HomeProductsRequest r) {
        Specification<Product> spec = Specification.where(ProductSpecs.isActive());
        if (r.category() != null) spec = spec.and(ProductSpecs.categoryEquals(r.category()));
        if (r.search() != null && !r.search().isBlank()) spec = spec.and(ProductSpecs.nameContains(r.search()));
        if (r.minPrice() != null) spec = spec.and(ProductSpecs.priceGte(r.minPrice()));
        if (r.maxPrice() != null) spec = spec.and(ProductSpecs.priceLte(r.maxPrice()));
        if (r.minRating() != null) spec = spec.and(ProductSpecs.ratingGte(r.minRating()));
        if (r.maxRating() != null) spec = spec.and(ProductSpecs.ratingLte(r.maxRating()));

        return productRepository.count(spec);
    }


    private void upsertAll(List<ProductSnapshot> snapshots) {
        for (ProductSnapshot s : snapshots) {
            Product p = productRepository
                    .findByVendorAndExternalProductId(s.getVendor(), s.getExternalProductId())
                    .orElseGet(() -> Product.builder()
                            .vendor(s.getVendor())
                            .externalProductId(s.getExternalProductId())
                            .status(ProductStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .build());

            // 덮어쓰기 (엔티티에 이런 메서드 만들어두는 걸 추천)
            p.applySnapshot(s);

            productRepository.save(p);
        }
    }

    @Transactional
    public Void saveRecentProduct(Long userId, Long productId, boolean isFavoritedSnapshot) {
        LocalDateTime now = LocalDateTime.now();

        RecentViewedProduct row = recentViewedProductRepository
                .findByUserIdAndProductIdAndStatus(userId, productId, ProductStatus.ACTIVE)
                .map(existing -> {
                    // 최신 시각으로 갱신
                    return RecentViewedProduct.builder()
                            .id(existing.getId())
                            .userId(existing.getUserId())
                            .productId(existing.getProductId())
                            .isFavoritedSnapshot(isFavoritedSnapshot)
                            .viewedAt(now)
                            .status(existing.getStatus())
                            .createdAt(existing.getCreatedAt())
                            .updatedAt(now)
                            .build();
                })
                .orElseGet(() -> {
                    // 처음 봄: 새로 저장
                    return RecentViewedProduct.builder()
                            .userId(userId)
                            .productId(productId)
                            .isFavoritedSnapshot(isFavoritedSnapshot)
                            .viewedAt(now)
                            .status(ProductStatus.ACTIVE)
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                });

        recentViewedProductRepository.save(row);
        return null;
    }

    @Transactional
    public List<HomeProductItemResponse> getRecentProducts(Long userId) {
        List<RecentViewedProduct> recents = recentViewedProductRepository
                .findByUserIdAndStatusOrderByViewedAtDesc(
                        userId,
                        ProductStatus.ACTIVE,
                        PageRequest.of(0, FIXED_SIZE)
                );

        if (recents.isEmpty()) return List.of();

        List<Long> productIds = recents.stream()
                .map(RecentViewedProduct::getProductId)
                .distinct()
                .toList();

        List<Product> products = productRepository.findByIdInAndStatus(productIds, ProductStatus.ACTIVE);
        Map<Long, Product> productById = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Set<Long> favoritedIds = productFavoriteRepository
                .findByUserIdAndProductIdInAndStatus(userId, productIds, FavoriteStatus.ACTIVE)
                .stream()
                .map(ProductFavorite::getProductId)
                .collect(Collectors.toSet());

        List<HomeProductItemResponse> result = new ArrayList<>();
        for (Long productId : productIds) {
            Product p = productById.get(productId);
            if (p == null) continue;

            result.add(HomeProductItemResponse.from(p, favoritedIds.contains(productId)));

            if (result.size() == FIXED_SIZE) break;
        }

        return result;
    }

    @Transactional
    public HomeProductsResult getHomeRecommendedProducts(Long userId, int page, int size) {
        List<RecentViewedProduct> recents = recentViewedProductRepository
                .findByUserIdAndStatusOrderByViewedAtDesc(userId, ProductStatus.ACTIVE, PageRequest.of(0, RECENT_CATEGORY_SOURCE_MAX));
        if(recents.isEmpty()){
            return fallbackByLatest(userId, page, size, null);
        }

        List<Long> recentProductIds = recents.stream()
                .map(RecentViewedProduct::getProductId)
                .distinct()
                .toList();
        List<ProductCategory> categories = productRepository.findDistinctCategoriesByIdsAndStatus(recentProductIds, ProductStatus.ACTIVE);
        if (categories.isEmpty()) {
            return fallbackByLatest(userId, page, size, null);
        }

        List<String> keywords = trendingCountRepository
                .findTop20ByStatusOrderByDailyCountDesc(SearchStatus.ACTIVE)
                .stream()
                .map(tc -> tc.getKeyword() == null ? "" : tc.getKeyword().trim().toLowerCase())
                .filter(s -> !s.isBlank())
                .distinct()
                .limit(TRENDING_KEYWORD_MAX)
                .toList();
        if (keywords.isEmpty()) {
            return fallbackByLatest(userId, page, size, categories);
        }

        Page<Product> pages = productRepository.findRecommendedByCategoriesAndTrendingKeywords(
                ProductStatus.ACTIVE,
                SearchStatus.ACTIVE,
                categories,
                keywords,
                PageRequest.of(page, size)
        );

        return toUserProductPage(userId, pages);
    }

    private HomeProductsResult fallbackByLatest(
            Long userId,
            int page,
            int size,
            List<ProductCategory> categoriesOrNull
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Product> resultPage;

        if (categoriesOrNull == null || categoriesOrNull.isEmpty()) {
            resultPage = productRepository.findAll(pageable);
        } else {
            resultPage = productRepository.findByCategoryInAndStatus(
                    categoriesOrNull,
                    ProductStatus.ACTIVE,
                    pageable
            );
        }

        return toUserProductPage(userId, resultPage);
    }

    private HomeProductsResult toUserProductPage(Long userId, Page<Product> page){
        List<Product> products = page.getContent();
        if (products.isEmpty()) {
            PageResult<HomeProductItemResponse> pr =
                    new PageResult<>(List.of(), page.getNumber(), page.getSize(), page.hasNext());
            return new HomeProductsResult(pr);
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();

        Set<Long> favoritedIds = (userId == null)
                ? Collections.emptySet()
                : productFavoriteRepository
                .findByUserIdAndProductIdInAndStatus(userId, productIds, FavoriteStatus.ACTIVE)
                .stream()
                .map(ProductFavorite::getProductId)
                .collect(Collectors.toSet());

        List<HomeProductItemResponse> items = products.stream()
                .map(p -> HomeProductItemResponse.from(p, favoritedIds.contains(p.getId())))
                .toList();

        PageResult<HomeProductItemResponse> pr =
                new PageResult<>(items, page.getNumber(), page.getSize(), page.hasNext());
        return new HomeProductsResult(pr);
    }

}

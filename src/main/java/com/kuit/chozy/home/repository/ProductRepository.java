package com.kuit.chozy.home.repository;

import com.kuit.chozy.home.entity.Product;
import com.kuit.chozy.home.entity.ProductCategory;
import com.kuit.chozy.home.entity.ProductStatus;
import com.kuit.chozy.home.entity.Vendor;
import com.kuit.chozy.likes.entity.FavoriteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findByVendorAndExternalProductId(Vendor vendor, Long externalProductId);
    List<Product> findByIdInAndStatus(List<Long> ids, ProductStatus status);

    @Query("""
        select distinct p.category
        from Product p
        where p.status = :status
          and p.id in :productIds
    """)
    List<ProductCategory> findDistinctCategoriesByIdsAndStatus(
            @Param("productIds") List<Long> productIds,
            @Param("status") ProductStatus status
    );

    @Query(
            value = """
        select p
        from Product p
        left join TrendingCount tc
          on tc.status = :searchStatus
         and tc.keyword in :keywords
         and lower(p.name) like concat('%', tc.keyword, '%')
        where p.status = :productStatus
          and p.category in :categories
        group by p
        order by coalesce(sum(tc.dailyCount), 0) desc, p.updatedAt desc
      """,
            countQuery = """
        select count(p)
        from Product p
        where p.status = :productStatus
          and p.category in :categories
      """
    )
    Page<Product> findRecommendedByCategoriesAndTrendingKeywords(
            @Param("productStatus") ProductStatus productStatus,
            @Param("searchStatus") com.kuit.chozy.home.entity.SearchStatus searchStatus,
            @Param("categories") List<ProductCategory> categories,
            @Param("keywords") List<String> keywords,
            Pageable pageable
    );

    Page<Product> findByCategoryInAndStatus(
            List<ProductCategory> categories,
            ProductStatus status,
            Pageable pageable
    );

    @Query("""
        select p
        from ProductFavorite f
        join Product p on p.id = f.productId
        where f.userId = :userId
          and f.status = :favStatus
          and p.status = :productStatus
          and (:search is null or :search = '' or lower(p.name) like concat('%', lower(:search), '%'))
        order by f.updatedAt desc
    """)
    Page<Product> findLikedProducts(
            @Param("userId") Long userId,
            @Param("favStatus") FavoriteStatus favStatus,
            @Param("productStatus") ProductStatus productStatus,
            @Param("search") String search,
            Pageable pageable
    );
}


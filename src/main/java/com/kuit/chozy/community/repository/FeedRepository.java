package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.Feed;
import com.kuit.chozy.community.domain.FeedContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    Page<Feed> findByContentTypeOrderByCreatedAtDesc(FeedContentType contentType, Pageable pageable);

    Page<Feed> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Feed> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds, Pageable pageable);

    Page<Feed> findByUserIdInAndContentTypeOrderByCreatedAtDesc(List<Long> userIds, FeedContentType contentType, Pageable pageable);

    @Query("""
        SELECT f FROM Feed f
        WHERE (:contentType IS NULL OR f.contentType = :contentType)
        AND (LOWER(f.text) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.title) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.vendor) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY f.createdAt DESC
        """)
    Page<Feed> findBySearchOrderByCreatedAtDesc(
            @Param("search") String search,
            @Param("contentType") FeedContentType contentType,
            Pageable pageable
    );

    @Query("""
        SELECT f FROM Feed f
        WHERE f.userId IN :userIds
        AND (:contentType IS NULL OR f.contentType = :contentType)
        AND (LOWER(f.text) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.title) LIKE LOWER(CONCAT('%', :search, '%'))
             OR LOWER(f.vendor) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY f.createdAt DESC
        """)
    Page<Feed> findByUserIdInAndSearchOrderByCreatedAtDesc(
            @Param("userIds") List<Long> userIds,
            @Param("search") String search,
            @Param("contentType") FeedContentType contentType,
            Pageable pageable
    );
}

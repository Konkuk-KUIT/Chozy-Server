package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.Feed;
import com.kuit.chozy.community.domain.FeedContentType;
import com.kuit.chozy.community.domain.FeedKind;
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
      AND (
           LOWER(COALESCE(f.content, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.quoteText, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.vendor, '')) LIKE LOWER(CONCAT('%', :search, '%'))
      )
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
      AND (
           LOWER(COALESCE(f.content, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.quoteText, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.vendor, '')) LIKE LOWER(CONCAT('%', :search, '%'))
      )
    ORDER BY f.createdAt DESC
""")
    Page<Feed> findByUserIdInAndSearchOrderByCreatedAtDesc(
            @Param("userIds") List<Long> userIds,
            @Param("search") String search,
            @Param("contentType") FeedContentType contentType,
            Pageable pageable
    );


    boolean existsByUserIdAndKindAndOriginalFeedId(Long userId, FeedKind kind, Long originalFeedId);

    Page<Feed> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Page<Feed> findByUserIdAndContentTypeOrderByCreatedAtDesc(
            Long userId,
            FeedContentType contentType,
            Pageable pageable
    );

    @Query("""
    SELECT f FROM Feed f
    WHERE f.userId = :userId
      AND f.contentType = :contentType
      AND (
           LOWER(COALESCE(f.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(COALESCE(f.quoteText, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(COALESCE(f.vendor, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    ORDER BY f.createdAt DESC
""")
    Page<Feed> findByUserIdAndSearchOrderByCreatedAtDesc(
            @Param("userId") Long userId,
            @Param("contentType") FeedContentType contentType,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // ---- cursor 기반 페이징 (목록 조회) ----

    @Query("SELECT f FROM Feed f WHERE (:cursorId IS NULL OR f.id < :cursorId) AND (:contentType IS NULL OR f.contentType = :contentType) ORDER BY f.id DESC")
    List<Feed> findForRecommendCursor(
            @Param("cursorId") Long cursorId,
            @Param("contentType") FeedContentType contentType,
            Pageable pageable
    );

    @Query("""
    SELECT f FROM Feed f
    WHERE (:cursorId IS NULL OR f.id < :cursorId) AND (:contentType IS NULL OR f.contentType = :contentType)
      AND (LOWER(COALESCE(f.content, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.quoteText, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.vendor, '')) LIKE LOWER(CONCAT('%', :search, '%')))
    ORDER BY f.id DESC
    """)
    List<Feed> findForRecommendCursorWithSearch(
            @Param("cursorId") Long cursorId,
            @Param("contentType") FeedContentType contentType,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT f FROM Feed f WHERE f.userId IN :userIds AND (:cursorId IS NULL OR f.id < :cursorId) AND (:contentType IS NULL OR f.contentType = :contentType) ORDER BY f.id DESC")
    List<Feed> findForFollowingCursor(
            @Param("userIds") List<Long> userIds,
            @Param("cursorId") Long cursorId,
            @Param("contentType") FeedContentType contentType,
            Pageable pageable
    );

    @Query("""
    SELECT f FROM Feed f
    WHERE f.userId IN :userIds AND (:cursorId IS NULL OR f.id < :cursorId) AND (:contentType IS NULL OR f.contentType = :contentType)
      AND (LOWER(COALESCE(f.content, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.quoteText, '')) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(COALESCE(f.vendor, '')) LIKE LOWER(CONCAT('%', :search, '%')))
    ORDER BY f.id DESC
    """)
    List<Feed> findForFollowingCursorWithSearch(
            @Param("userIds") List<Long> userIds,
            @Param("cursorId") Long cursorId,
            @Param("contentType") FeedContentType contentType,
            @Param("search") String search,
            Pageable pageable
    );
}

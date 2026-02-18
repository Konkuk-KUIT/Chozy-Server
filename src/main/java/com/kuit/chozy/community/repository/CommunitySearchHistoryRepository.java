package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.domain.CommunitySearchHistory;
import com.kuit.chozy.home.entity.SearchHistory;
import com.kuit.chozy.home.entity.SearchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommunitySearchHistoryRepository extends JpaRepository<CommunitySearchHistory, Long> {

    Optional<CommunitySearchHistory> findByUserIdAndKeywordAndStatus(
            Long userId,
            String keyword,
            SearchStatus status
    );

    List<CommunitySearchHistory> findTop10ByUserIdAndStatusOrderByUpdatedAtDesc(
            Long userId,
            SearchStatus status
    );

    List<CommunitySearchHistory> findTop10ByStatusAndKeywordContainingOrderByCountTotalDescUpdatedAtDesc(
            SearchStatus status, String keyword
    );

    List<CommunitySearchHistory> findByUserIdAndStatus(Long userId, SearchStatus status);

    Optional<SearchHistory> findByUserIdAndIdAndStatus(Long userId, Long id, SearchStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update LikeSearchHistory h
        set h.status = :inactive, h.updatedAt = :now
        where h.userId = :userId and h.status = :active
        """)
    int deactivateAllByUserId(@Param("userId") Long userId,
                              @Param("active") SearchStatus active,
                              @Param("inactive") SearchStatus inactive,
                              @Param("now") LocalDateTime now);
}

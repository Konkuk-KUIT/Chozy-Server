package com.kuit.chozy.home.repository;

import com.kuit.chozy.home.entity.SearchHistory;
import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.likes.entity.LikeSearchHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Optional<SearchHistory> findByUserIdAndKeywordAndStatus(
            Long userId,
            String keyword,
            SearchStatus status
    );

    List<SearchHistory> findTop10ByUserIdAndStatusOrderByUpdatedAtDesc(
            Long userId,
            SearchStatus status
    );

    List<SearchHistory> findTop10ByStatusAndKeywordContainingOrderByCountTotalDescUpdatedAtDesc(
            SearchStatus status, String keyword
    );

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

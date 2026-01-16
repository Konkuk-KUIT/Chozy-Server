package com.kuit.chozy.home.repository;

import com.kuit.chozy.home.entity.SearchHistory;
import com.kuit.chozy.home.entity.SearchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
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

}

package com.kuit.chozy.community.repository;

import com.kuit.chozy.community.entity.CommunitySearchHistory;
import com.kuit.chozy.home.entity.SearchHistory;
import com.kuit.chozy.home.entity.SearchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

}

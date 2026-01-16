package com.kuit.chozy.home.repository;

import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.home.entity.TrendingCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrendingCountRepository extends JpaRepository<TrendingCount, Long> {

    List<TrendingCount> findTop10ByStatusOrderByRankTodayAsc(
            SearchStatus status
    );

    List<TrendingCount> findByStatus(SearchStatus status);

    List<TrendingCount> findTop10ByStatusOrderByDailyCountDesc(SearchStatus status);

    Optional<TrendingCount> findByKeywordAndStatus(String keyword, SearchStatus searchStatus);
}

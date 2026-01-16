package com.kuit.chozy.home.service;

import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.home.entity.TrendingCount;
import com.kuit.chozy.home.repository.TrendingCountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendingService {

    private final TrendingCountRepository trendingCountRepository;

    @Transactional
    public void rebuildDailyTrending() {
        List<TrendingCount> trendingCounts = trendingCountRepository.findByStatus(SearchStatus.ACTIVE);

        for (TrendingCount tc : trendingCounts) {
            tc.moveTodayToYesterday();
        }

        List<TrendingCount> topTrendingCounts = trendingCountRepository.findTop10ByStatusOrderByDailyCountDesc(SearchStatus.ACTIVE);

        for (TrendingCount tc : trendingCounts) {
            tc.setTodayRank(0);
        }

        for (int i = 0; i <= topTrendingCounts.size(); i++) {
            topTrendingCounts.get(i).setTodayRank(i+1);
        }

        for (TrendingCount tc : trendingCounts) {
            tc.resetDailyCount();
        }

    }
}

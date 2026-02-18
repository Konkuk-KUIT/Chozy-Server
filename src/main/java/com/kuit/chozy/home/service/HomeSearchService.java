package com.kuit.chozy.home.service;

import com.kuit.chozy.home.dto.response.KeywordResponse;
import com.kuit.chozy.home.dto.response.PopularSearchKeywordResponse;
import com.kuit.chozy.home.dto.response.RecentSearchKeywordResponse;
import com.kuit.chozy.home.dto.response.RecommendSearchKeywordResponse;
import com.kuit.chozy.home.entity.SearchHistory;
import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.home.entity.TrendingCount;
import com.kuit.chozy.home.repository.SearchHistoryRepository;
import com.kuit.chozy.home.repository.TrendingCountRepository;
import com.kuit.chozy.likes.entity.LikeSearchHistory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeSearchService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final TrendingCountRepository trendingCountRepository;

    // 최근 검색어 조회 기능
    public RecentSearchKeywordResponse getRecentSearchKeyword(Long userId){
        List<KeywordResponse> histories =
                searchHistoryRepository.findTop10ByUserIdAndStatusOrderByUpdatedAtDesc(
                        userId,
                        SearchStatus.ACTIVE
                )
                        .stream()
                        .map(h -> new KeywordResponse(h.getId(), h.getKeyword()))
                        .toList();
        return new RecentSearchKeywordResponse(histories);
    }

    // 인기 검색어 조회 기능
    public List<PopularSearchKeywordResponse> getPopularSearchKeyword() {
        return trendingCountRepository.findTop10ByStatusOrderByRankTodayAsc(SearchStatus.ACTIVE)
                .stream()
                .map(k -> new PopularSearchKeywordResponse(k.getId(), k.getKeyword(), k.getRankYesterday(), k.getRankToday()))
                .toList();
    }

    // 검색어 자동 완성 기능
    public RecommendSearchKeywordResponse getRecommendSearchKeyword(String keyword) {
        List<KeywordResponse> keywords =
                searchHistoryRepository.findTop10ByStatusAndKeywordContainingOrderByCountTotalDescUpdatedAtDesc(
                        SearchStatus.ACTIVE, keyword
                )
                        .stream()
                        .map(k -> new KeywordResponse(k.getId(), k.getKeyword()))
                        .toList();
        return new RecommendSearchKeywordResponse(keywords);
    }

    // 검색어 저장 기능
    @Transactional
    public void saveSearchKeyword(Long userId, String keyword) {
        String normalizedKeyword = keyword.trim().toLowerCase();

        // SearchHistory 저장
        SearchHistory history = searchHistoryRepository.findByUserIdAndKeywordAndStatus(
                userId, normalizedKeyword, SearchStatus.ACTIVE
        ).orElseGet(() -> SearchHistory.create(userId, normalizedKeyword));

        history.increaseCount();
        searchHistoryRepository.save(history);

        // TrendingCount daily_count 증가
        TrendingCount tc = trendingCountRepository.findByKeywordAndStatus(
                keyword, SearchStatus.ACTIVE
        ).orElseGet(() -> TrendingCount.create(keyword, 9999));

        tc.increaseDailyCount();
        trendingCountRepository.save(tc);
    }

    @Transactional
    public void deleteRecentSearchKeyword(Long userId, Long historyId) {
        SearchHistory history = searchHistoryRepository
                .findByUserIdAndIdAndStatus(userId, historyId, SearchStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("최근 검색어가 없거나 이미 삭제됨"));

        history.deactivate();
    }

    @Transactional
    public void deleteAllRecentSearchKeywords(Long userId) {
        searchHistoryRepository.deactivateAllByUserId(
                userId,
                SearchStatus.ACTIVE,
                SearchStatus.INACTIVE,
                LocalDateTime.now()
        );
    }
}

package com.kuit.chozy.likes.service;

import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.likes.dto.response.KeywordResponse;
import com.kuit.chozy.likes.dto.response.RecentSearchKeywordResponse;
import com.kuit.chozy.likes.dto.response.RecommendSearchKeywordResponse;
import com.kuit.chozy.likes.entity.LikeSearchHistory;
import com.kuit.chozy.likes.repository.LikeSearchHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeSearchService {

    private final LikeSearchHistoryRepository likeSearchHistoryRepository;

    // 최근 검색어 조회 기능
    public RecentSearchKeywordResponse getRecentSearchKeyword(Long userId){
        List<KeywordResponse> histories =
                likeSearchHistoryRepository.findTop10ByUserIdAndStatusOrderByUpdatedAtDesc(
                                userId,
                                SearchStatus.ACTIVE
                        )
                        .stream()
                        .map(h -> new KeywordResponse(h.getId(), h.getKeyword()))
                        .toList();
        return new RecentSearchKeywordResponse(histories);
    }

    // 검색어 자동 완성 기능
    public RecommendSearchKeywordResponse getRecommendSearchKeyword(String keyword) {
        List<KeywordResponse> keywords =
                likeSearchHistoryRepository.findTop10ByStatusAndKeywordContainingOrderByCountTotalDescUpdatedAtDesc(
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
        LikeSearchHistory history = likeSearchHistoryRepository.findByUserIdAndKeywordAndStatus(
                userId, keyword, SearchStatus.ACTIVE
        ).orElseGet(() -> LikeSearchHistory.create(userId, normalizedKeyword));

        history.increaseCount();
        likeSearchHistoryRepository.save(history);
    }

    @Transactional
    public void deleteRecentSearchKeyword(Long userId, Long historyId) {
        LikeSearchHistory history = likeSearchHistoryRepository
                .findByUserIdAndIdAndStatus(userId, historyId, SearchStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("최근 검색어가 없거나 이미 삭제됨"));

        history.deactivate();
    }

    @Transactional
    public void deleteAllRecentSearchKeywords(Long userId) {
        likeSearchHistoryRepository.deactivateAllByUserId(
                userId,
                SearchStatus.ACTIVE,
                SearchStatus.INACTIVE,
                LocalDateTime.now()
        );
    }


}

package com.kuit.chozy.home.dto.response;

import com.kuit.chozy.home.entity.TrendingCount;

public record PopularSearchKeywordResponse(
        Long keywordId,
        String keyword,
        int previousRank,
        int currentRank
) {
    public static PopularSearchKeywordResponse from(TrendingCount trendingCount) {
        return new PopularSearchKeywordResponse(trendingCount.getId(), trendingCount.getKeyword(), trendingCount.getRankYesterday(), trendingCount.getRankToday());
    }
}

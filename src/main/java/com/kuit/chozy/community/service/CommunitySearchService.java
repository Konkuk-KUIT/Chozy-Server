package com.kuit.chozy.community.service;

import com.kuit.chozy.community.dto.response.RecentViewedProfileResponse;
import com.kuit.chozy.community.dto.response.UserLoginIdRecommendResponse;
import com.kuit.chozy.community.domain.CommunitySearchHistory;
import com.kuit.chozy.community.domain.RecentViewedProfile;
import com.kuit.chozy.community.repository.CommunitySearchHistoryRepository;
import com.kuit.chozy.community.repository.CommunityUserRepository;
import com.kuit.chozy.community.repository.RecentViewedProfileRepository;
import com.kuit.chozy.home.dto.response.KeywordResponse;
import com.kuit.chozy.home.dto.response.RecentSearchKeywordResponse;
import com.kuit.chozy.home.dto.response.RecommendSearchKeywordResponse;
import com.kuit.chozy.home.entity.SearchHistory;
import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommunitySearchService {

    private final CommunitySearchHistoryRepository communitySearchHistoryRepository;
    private final CommunityUserRepository userRepository;
    private final RecentViewedProfileRepository recentViewedProfileRepository;

    // 최근 검색어 조회 기능
    public RecentSearchKeywordResponse getRecentSearchKeyword(Long userId){
        List<KeywordResponse> histories =
                communitySearchHistoryRepository.findTop10ByUserIdAndStatusOrderByUpdatedAtDesc(
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
                communitySearchHistoryRepository.findTop10ByStatusAndKeywordContainingOrderByCountTotalDescUpdatedAtDesc(
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
        CommunitySearchHistory history = communitySearchHistoryRepository.findByUserIdAndKeywordAndStatus(
                userId, keyword, SearchStatus.ACTIVE
        ).orElseGet(() -> CommunitySearchHistory.create(userId, normalizedKeyword));

        history.increaseCount();
        communitySearchHistoryRepository.save(history);
    }

    // 아이디 자동 완성 기능
    @Transactional
    public List<UserLoginIdRecommendResponse> getRecommendLoginId(String loginId) {
        String normalizedLoginId = loginId.trim();

        return userRepository.findTop10ByStatusAndIsAccountPublicTrueAndLoginIdContainingOrderByLoginIdAsc(
                        UserStatus.ACTIVE, normalizedLoginId
                )
                        .stream()
                        .map(u -> new UserLoginIdRecommendResponse(u.getLoginId(), u.getProfileImageUrl()))
                        .toList();
    }

    @Transactional
    public void saveProfile(Long userId, long profileId) {
        if(userId.equals(profileId)) return;

        User viewer = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
        User visitedUser = userRepository.findById(profileId).orElseThrow(() -> new IllegalArgumentException("방문 대상이 존재하지 않습니다."));

        RecentViewedProfile profile = recentViewedProfileRepository.findByViewer_IdAndVisitedUser_Id(
                userId, profileId
        ).orElseGet(() -> RecentViewedProfile.create(viewer, visitedUser));

        profile.activate();
        recentViewedProfileRepository.save(profile);
    }

    public List<RecentViewedProfileResponse> getRecentProfiles(Long userId) {

        return recentViewedProfileRepository.findTop8ByViewer_IdAndStatusOrderByUpdatedAtDesc(
                userId, UserStatus.ACTIVE
        )
                .stream()
                .map((r) -> {
                    User visitedUser = r.getVisitedUser();
                    return new RecentViewedProfileResponse(
                            visitedUser.getId(),
                            visitedUser.getNickname(),
                            visitedUser.getProfileImageUrl()
                    );
                })
                .toList();
    }

    @Transactional
    public void deleteRecentSearchKeyword(Long userId, Long historyId) {
        CommunitySearchHistory history = communitySearchHistoryRepository
                .findByUserIdAndIdAndStatus(userId, historyId, SearchStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("최근 검색어가 없거나 이미 삭제됨"));

        history.deactivate();
    }

    @Transactional
    public void deleteAllRecentSearchKeywords(Long userId) {
        communitySearchHistoryRepository.deactivateAllByUserId(
                userId,
                SearchStatus.ACTIVE,
                SearchStatus.INACTIVE,
                LocalDateTime.now()
        );
    }
}

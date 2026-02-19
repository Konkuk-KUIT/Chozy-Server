package com.kuit.chozy.me.service;

import com.kuit.chozy.community.domain.CommunitySearchHistory;
import com.kuit.chozy.community.domain.Feed;
import com.kuit.chozy.community.domain.FeedBookmark;
import com.kuit.chozy.community.domain.FeedReaction;
import com.kuit.chozy.community.domain.ReactionType;
import com.kuit.chozy.community.dto.response.FeedItemResponse;
import com.kuit.chozy.community.repository.CommunitySearchHistoryRepository;
import com.kuit.chozy.community.repository.FeedBookmarkRepository;
import com.kuit.chozy.community.repository.FeedReactionRepository;
import com.kuit.chozy.community.repository.FeedRepository;
import com.kuit.chozy.community.service.CommunityFeedService;
import com.kuit.chozy.community.service.CommunitySearchService;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.home.entity.SearchStatus;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.dto.response.*;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.dto.FollowStatus;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;
    private final FeedBookmarkRepository feedBookmarkRepository;
    private final FeedReactionRepository feedReactionRepository;
    private final CommunitySearchHistoryRepository communitySearchHistoryRepository;
    private final CommunityFeedService communityFeedService;
    private final CommunitySearchService communitySearchService;

    @Transactional(readOnly = true)
    public ProfileResponseDto getMyProfile(Long userId) {
        User user = getActiveUser(userId);
        long reviewCount = feedRepository.countByUserId(user.getId());
        long followerCount = followRepository.countByFollowingIdAndStatus(user.getId(), FollowStatus.FOLLOWING);
        long followingCount = followRepository.countByFollowerIdAndStatus(user.getId(), FollowStatus.FOLLOWING);
        return ProfileResponseDto.from(user, reviewCount, followerCount, followingCount);
    }

    @Transactional
    public ProfileResponseDto updateMyProfile(
            Long userId,
            ProfileUpdateDto request
    ) {
        User user = getActiveUser(userId);

        if (request.getNickname() != null)
            user.setNickname(request.getNickname());

        if (request.getStatusMessage() != null)
            user.setStatusMessage(request.getStatusMessage());

        if (request.getProfileImageUrl() != null)
            user.setProfileImageUrl(request.getProfileImageUrl());

        if (request.getIsAccountPublic() != null)
            user.setIsAccountPublic(request.getIsAccountPublic());

        if (request.getBirthDate() != null)
            user.setBirthDate(request.getBirthDate());

        if (request.getHeight() != null)
            user.setHeight(request.getHeight());

        if (request.getWeight() != null)
            user.setWeight(request.getWeight());

        if (request.getIsBirthPublic() != null)
            user.setIsBirthPublic(request.getIsBirthPublic());

        if (request.getIsHeightPublic() != null)
            user.setIsHeightPublic(request.getIsHeightPublic());

        if (request.getIsWeightPublic() != null)
            user.setIsWeightPublic(request.getIsWeightPublic());

        long reviewCount = feedRepository.countByUserId(user.getId());
        long followerCount = followRepository.countByFollowingIdAndStatus(user.getId(), FollowStatus.FOLLOWING);
        long followingCount = followRepository.countByFollowerIdAndStatus(user.getId(), FollowStatus.FOLLOWING);
        return ProfileResponseDto.from(user, reviewCount, followerCount, followingCount);
    }

    private static final int MAX_PAGE_SIZE = 50;

    /** 타인 조회 시 차단 관계만 검증 (프로필은 비공개여도 닉네임·게시글 수·팔로워/팔로잉 등 노출) */
    private void validateBlockRelation(Long viewerId, Long targetUserId) {
        if (viewerId.equals(targetUserId)) {
            return;
        }
        if (blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(viewerId, targetUserId)
                || blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(targetUserId, viewerId)) {
            throw new ApiException(ErrorCode.BLOCK_RELATION_EXISTS);
        }
    }

    /** 타인 피드/검색 조회 시 차단 + 비공개 검증 (비공개 계정은 팔로우한 경우에만 피드 노출) */
    private void validateTargetFeedAccess(Long viewerId, Long targetUserId, User targetUser) {
        validateBlockRelation(viewerId, targetUserId);
        if (viewerId.equals(targetUserId)) {
            return;
        }
        if (Boolean.FALSE.equals(targetUser.getIsAccountPublic())) {
            boolean following = followRepository.existsByFollowerIdAndFollowingIdAndStatus(
                    viewerId, targetUserId, FollowStatus.FOLLOWING);
            if (!following) {
                throw new ApiException(ErrorCode.PRIVATE_ACCOUNT_FORBIDDEN);
            }
        }
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getTargetProfile(Long viewerId, Long targetUserId) {
        if (viewerId.equals(targetUserId)) {
            return getMyProfile(targetUserId);
        }
        User targetUser = getActiveUser(targetUserId);
        validateBlockRelation(viewerId, targetUserId);
        long reviewCount = feedRepository.countByUserId(targetUser.getId());
        long followerCount = followRepository.countByFollowingIdAndStatus(targetUser.getId(), FollowStatus.FOLLOWING);
        long followingCount = followRepository.countByFollowerIdAndStatus(targetUser.getId(), FollowStatus.FOLLOWING);
        return ProfileResponseDto.fromForOtherUser(targetUser, reviewCount, followerCount, followingCount);
    }

    @Transactional(readOnly = true)
    public MeFeedsPageResponse getTargetFeeds(Long viewerId, Long targetUserId, int page, int size, String sort) {
        if (viewerId.equals(targetUserId)) {
            return getMyFeeds(targetUserId, page, size, sort);
        }
        User targetUser = getActiveUser(targetUserId);
        validateTargetFeedAccess(viewerId, targetUserId, targetUser);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Feed> feedPage = feedRepository.findByUserIdOrderByCreatedAtDesc(targetUser.getId(), pageable);
        List<FeedItemResponse> feeds = communityFeedService.toFeedItemResponses(feedPage.getContent(), viewerId);
        return MeFeedsPageResponse.builder()
                .feeds(feeds)
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public MeFeedsPageResponse searchTargetFeeds(Long viewerId, Long targetUserId, String query, int page, int size, String sort) {
        if (viewerId.equals(targetUserId)) {
            return searchMyFeeds(targetUserId, query, page, size, sort);
        }
        User targetUser = getActiveUser(targetUserId);
        validateTargetFeedAccess(viewerId, targetUserId, targetUser);

        if (!StringUtils.hasText(query)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Feed> feedPage = feedRepository.findByUserIdAndSearchOrderByCreatedAtDesc(
                targetUser.getId(),
                null,
                query.trim(),
                pageable
        );
        List<FeedItemResponse> feeds = communityFeedService.toFeedItemResponses(feedPage.getContent(), viewerId);
        return MeFeedsPageResponse.builder()
                .feeds(feeds)
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public MeFeedsPageResponse getMyFeeds(Long userId, int page, int size, String sort) {
        User user = getActiveUser(userId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Feed> feedPage = feedRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        List<FeedItemResponse> feeds = communityFeedService.toFeedItemResponses(feedPage.getContent(), userId);
        return MeFeedsPageResponse.builder()
                .feeds(feeds)
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .build();
    }

    /** 마이피드 검색 (호출 시 검색어가 검색 기록에 저장됨) */
    @Transactional
    public MeFeedsPageResponse searchMyFeeds(Long userId, String query, int page, int size, String sort) {
        User user = getActiveUser(userId);

        if (!StringUtils.hasText(query)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        communitySearchService.saveSearchKeyword(userId, query.trim());

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Feed> feedPage = feedRepository.findByUserIdAndSearchOrderByCreatedAtDesc(
                user.getId(),
                null,
                query.trim(),
                pageable
        );

        List<FeedItemResponse> feeds = communityFeedService.toFeedItemResponses(feedPage.getContent(), userId);
        return MeFeedsPageResponse.builder()
                .feeds(feeds)
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public MeLikedFeedsPageResponse getMyLikedFeeds(Long userId, int page, int size, String sort) {
        User user = getActiveUser(userId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<FeedReaction> reactionPage = feedReactionRepository.findByUserIdAndReactionTypeOrderByCreatedAtDesc(
                user.getId(),
                ReactionType.LIKE,
                pageable
        );

        if (reactionPage.isEmpty()) {
            return MeLikedFeedsPageResponse.builder()
                    .feeds(List.of())
                    .page(reactionPage.getNumber())
                    .size(reactionPage.getSize())
                    .totalElements(reactionPage.getTotalElements())
                    .totalPages(reactionPage.getTotalPages())
                    .hasNext(reactionPage.hasNext())
                    .build();
        }

        List<FeedReaction> reactions = reactionPage.getContent();
        List<Long> feedIds = reactions.stream().map(FeedReaction::getFeedId).toList();

        Map<Long, Feed> feedMap = feedRepository.findAllById(feedIds).stream()
                .collect(Collectors.toMap(Feed::getId, f -> f));

        List<FeedLikedItemResponse> feeds = new ArrayList<>();
        for (FeedReaction reaction : reactions) {
            Feed feed = feedMap.get(reaction.getFeedId());
            if (feed == null) continue;

            List<FeedItemResponse> itemList = communityFeedService.toFeedItemResponses(List.of(feed), userId);
            if (itemList.isEmpty()) continue;

            FeedItemResponse r = itemList.get(0);
            feeds.add(FeedLikedItemResponse.builder()
                    .feedId(r.getFeedId())
                    .kind(r.getKind())
                    .contentType(r.getContentType())
                    .createdAt(r.getCreatedAt())
                    .user(r.getUser())
                    .contents(r.getContents())
                    .counts(r.getCounts())
                    .myState(r.getMyState())
                    .likedAt(reaction.getCreatedAt())
                    .build());
        }

        return MeLikedFeedsPageResponse.builder()
                .feeds(feeds)
                .page(reactionPage.getNumber())
                .size(reactionPage.getSize())
                .totalElements(reactionPage.getTotalElements())
                .totalPages(reactionPage.getTotalPages())
                .hasNext(reactionPage.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public RecentSearchesResponse getRecentSearches(Long userId) {
        getActiveUser(userId);

        List<CommunitySearchHistory> histories = communitySearchHistoryRepository
                .findTop10ByUserIdAndStatusOrderByUpdatedAtDesc(userId, SearchStatus.ACTIVE);

        List<RecentSearchItemResponse> searches = histories.stream()
                .map(h -> RecentSearchItemResponse.builder()
                        .searchedId(h.getId())
                        .query(h.getKeyword())
                        .searchedAt(h.getUpdatedAt())
                        .build())
                .toList();

        return RecentSearchesResponse.builder()
                .searches(searches)
                .build();
    }

    /** GET /me/searches 검색 (호출 시 검색어가 검색 기록에 저장되어 /me/searches/recent에 반영됨) */
    @Transactional
    public MeSearchResultResponse searchMyFeedsForMe(Long userId, String query, int page, int size) {
        MeFeedsPageResponse pageResult = searchMyFeeds(userId, query, page, size, "latest");
        return MeSearchResultResponse.builder()
                .query(query)
                .feeds(pageResult.getFeeds())
                .build();
    }

    @Transactional
    public DeleteSearchResponse deleteSearch(Long userId, Long searchedId) {
        getActiveUser(userId);

        CommunitySearchHistory history = communitySearchHistoryRepository.findById(searchedId)
                .orElseThrow(() -> new ApiException(ErrorCode.SEARCH_HISTORY_NOT_FOUND));

        if (!history.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.SEARCH_HISTORY_FORBIDDEN);
        }

        history.delete();
        communitySearchHistoryRepository.save(history);

        return DeleteSearchResponse.builder()
                .deletedSearchId(searchedId)
                .build();
    }

    @Transactional
    public DeleteAllSearchesResponse deleteAllSearches(Long userId) {
        getActiveUser(userId);

        List<CommunitySearchHistory> histories = communitySearchHistoryRepository
                .findByUserIdAndStatus(userId, SearchStatus.ACTIVE);

        for (CommunitySearchHistory h : histories) {
            h.delete();
        }
        communitySearchHistoryRepository.saveAll(histories);

        return DeleteAllSearchesResponse.builder()
                .deletedCount(histories.size())
                .build();
    }

    @Transactional(readOnly = true)
    public MeBookmarksPageResponse getMyBookmarks(Long userId, int page, int size) {
        User user = getActiveUser(userId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<FeedBookmark> bookmarkPage = feedBookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        if (bookmarkPage.isEmpty()) {
            return MeBookmarksPageResponse.builder()
                    .feeds(List.of())
                    .page(bookmarkPage.getNumber())
                    .size(bookmarkPage.getSize())
                    .totalElements(bookmarkPage.getTotalElements())
                    .totalPages(bookmarkPage.getTotalPages())
                    .hasNext(bookmarkPage.hasNext())
                    .build();
        }

        List<FeedBookmark> bookmarks = bookmarkPage.getContent();
        List<Long> feedIds = bookmarks.stream().map(FeedBookmark::getFeedId).toList();
        Map<Long, Feed> feedMap = feedRepository.findAllById(feedIds).stream()
                .collect(Collectors.toMap(Feed::getId, f -> f));

        List<FeedBookmarkItemResponse> feeds = new ArrayList<>();
        for (FeedBookmark bookmark : bookmarks) {
            Feed feed = feedMap.get(bookmark.getFeedId());
            if (feed == null) continue;

            List<FeedItemResponse> itemList = communityFeedService.toFeedItemResponses(List.of(feed), userId);
            if (itemList.isEmpty()) continue;

            FeedItemResponse r = itemList.get(0);
            feeds.add(FeedBookmarkItemResponse.builder()
                    .feedId(r.getFeedId())
                    .kind(r.getKind())
                    .contentType(r.getContentType())
                    .createdAt(r.getCreatedAt())
                    .user(r.getUser())
                    .contents(r.getContents())
                    .counts(r.getCounts())
                    .myState(r.getMyState())
                    .bookmarkedAt(bookmark.getCreatedAt())
                    .build());
        }

        return MeBookmarksPageResponse.builder()
                .feeds(feeds)
                .page(bookmarkPage.getNumber())
                .size(bookmarkPage.getSize())
                .totalElements(bookmarkPage.getTotalElements())
                .totalPages(bookmarkPage.getTotalPages())
                .hasNext(bookmarkPage.hasNext())
                .build();
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        return user;
    }
}
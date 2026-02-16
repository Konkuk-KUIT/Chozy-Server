package com.kuit.chozy.me.service;

import com.kuit.chozy.community.domain.Feed;
import com.kuit.chozy.community.domain.FeedBookmark;
import com.kuit.chozy.community.domain.FeedContentType;
import com.kuit.chozy.community.dto.response.FeedItemResponse;
import com.kuit.chozy.community.repository.FeedBookmarkRepository;
import com.kuit.chozy.community.repository.FeedRepository;
import com.kuit.chozy.community.service.CommunityFeedService;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.dto.response.FeedBookmarkItemResponse;
import com.kuit.chozy.me.dto.response.MeBookmarksPageResponse;
import com.kuit.chozy.me.dto.response.MeReviewsPageResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
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
    private final FeedBookmarkRepository feedBookmarkRepository;
    private final CommunityFeedService communityFeedService;

    @Transactional(readOnly = true)
    public ProfileResponseDto getMyProfile(Long userId) {
        User user = getActiveUser(userId);
        return ProfileResponseDto.from(user);
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
            user.setAccountPublic(request.getIsAccountPublic());

        if (request.getBirthDate() != null)
            user.setBirthDate(request.getBirthDate());

        if (request.getHeight() != null)
            user.setHeight(request.getHeight());

        if (request.getWeight() != null)
            user.setWeight(request.getWeight());

        if (request.getIsBirthPublic() != null)
            user.setBirthPublic(request.getIsBirthPublic());

        if (request.getIsHeightPublic() != null)
            user.setHeightPublic(request.getIsHeightPublic());

        if (request.getIsWeightPublic() != null)
            user.setHeightPublic(request.getIsWeightPublic());

        return ProfileResponseDto.from(user);
    }

    private static final int MAX_PAGE_SIZE = 50;

    @Transactional(readOnly = true)
    public MeReviewsPageResponse getMyReviews(Long userId, int page, int size, String sort) {
        User user = getActiveUser(userId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Feed> feedPage = feedRepository.findByUserIdAndContentTypeOrderByCreatedAtDesc(
                user.getId(),
                FeedContentType.REVIEW,
                pageable
        );

        List<FeedItemResponse> feeds = communityFeedService.toFeedItemResponses(feedPage.getContent(), userId);
        return MeReviewsPageResponse.builder()
                .feeds(feeds)
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public MeReviewsPageResponse searchMyReviews(Long userId, String keyword, int page, int size, String sort) {
        User user = getActiveUser(userId);

        if (!StringUtils.hasText(keyword)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<Feed> feedPage = feedRepository.findByUserIdAndSearchOrderByCreatedAtDesc(
                user.getId(),
                FeedContentType.REVIEW,
                keyword.trim(),
                pageable
        );

        List<FeedItemResponse> feeds = communityFeedService.toFeedItemResponses(feedPage.getContent(), userId);
        return MeReviewsPageResponse.builder()
                .feeds(feeds)
                .page(feedPage.getNumber())
                .size(feedPage.getSize())
                .totalElements(feedPage.getTotalElements())
                .totalPages(feedPage.getTotalPages())
                .hasNext(feedPage.hasNext())
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
package com.kuit.chozy.community.service;

import com.kuit.chozy.community.dto.response.*;
import com.kuit.chozy.community.domain.*;
import com.kuit.chozy.community.repository.FeedBookmarkRepository;
import com.kuit.chozy.community.repository.FeedReactionRepository;
import com.kuit.chozy.community.repository.FeedRepostRepository;
import com.kuit.chozy.community.repository.FeedRepository;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityFeedService {

    private static final int MAX_PAGE_SIZE = 50;

    private final FeedRepository feedRepository;
    private final FeedReactionRepository feedReactionRepository;
    private final FeedRepostRepository feedRepostRepository;
    private final FeedBookmarkRepository feedBookmarkRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public List<FeedItemResponse> getFeeds(
            Long userId,
            FeedTab tab,
            String contentTypeParam,
            String search,
            int page,
            int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        FeedContentType contentType = parseContentType(contentTypeParam);
        List<Feed> feeds;

        if (StringUtils.hasText(search)) {
            feeds = getFeedsBySearch(search.trim(), tab, contentType, userId, pageable);
        } else {
            feeds = getFeedsWithoutSearch(tab, contentType, userId, pageable);
        }

        if (feeds.isEmpty()) {
            return List.of();
        }

        return buildFeedItemResponses(feeds, userId);
    }

    private FeedContentType parseContentType(String contentTypeParam) {
        if (contentTypeParam == null || "ALL".equalsIgnoreCase(contentTypeParam)) {
            return null;
        }
        return FeedContentType.valueOf(contentTypeParam.toUpperCase());
    }

    private List<Feed> getFeedsWithoutSearch(FeedTab tab, FeedContentType contentType, Long userId, Pageable pageable) {
        if (tab == FeedTab.FOLLOWING) {
            List<Long> followingUserIds = followRepository.findByFollowerId(userId, Pageable.unpaged())
                    .getContent().stream()
                    .map(f -> f.getFollowingId())
                    .toList();
            if (followingUserIds.isEmpty()) {
                return List.of();
            }
            if (contentType == null) {
                return feedRepository.findByUserIdInOrderByCreatedAtDesc(followingUserIds, pageable).getContent();
            }
            return feedRepository.findByUserIdInAndContentTypeOrderByCreatedAtDesc(followingUserIds, contentType, pageable).getContent();
        } else {
            if (contentType == null) {
                return feedRepository.findAllByOrderByCreatedAtDesc(pageable).getContent();
            }
            return feedRepository.findByContentTypeOrderByCreatedAtDesc(contentType, pageable).getContent();
        }
    }

    private List<Feed> getFeedsBySearch(String search, FeedTab tab, FeedContentType contentType, Long userId, Pageable pageable) {
        if (tab == FeedTab.FOLLOWING) {
            List<Long> followingUserIds = followRepository.findByFollowerId(userId, Pageable.unpaged())
                    .getContent().stream()
                    .map(f -> f.getFollowingId())
                    .toList();
            if (followingUserIds.isEmpty()) {
                return List.of();
            }
            return feedRepository.findByUserIdInAndSearchOrderByCreatedAtDesc(followingUserIds, search, contentType, pageable).getContent();
        } else {
            return feedRepository.findBySearchOrderByCreatedAtDesc(search, contentType, pageable).getContent();
        }
    }

    private List<FeedItemResponse> buildFeedItemResponses(List<Feed> feeds, Long currentUserId) {
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();
        Set<Long> authorIds = feeds.stream().map(Feed::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findByIdIn(new ArrayList<>(authorIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, FeedReaction> reactionMap = feedReactionRepository.findByUserIdAndFeedIdIn(currentUserId, feedIds).stream()
                .collect(Collectors.toMap(FeedReaction::getFeedId, r -> r));
        Set<Long> repostedFeedIds = feedRepostRepository.findByUserIdAndSourceFeedIdIn(currentUserId, feedIds).stream()
                .map(FeedRepost::getSourceFeedId).collect(Collectors.toSet());
        Set<Long> bookmarkedFeedIds = feedBookmarkRepository.findByUserIdAndFeedIdIn(currentUserId, feedIds).stream()
                .map(FeedBookmark::getFeedId).collect(Collectors.toSet());

        Set<Long> quoteFeedIds = feeds.stream().map(Feed::getQuoteFeedId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Feed> quoteFeedMap = quoteFeedIds.isEmpty() ? Map.of()
                : feedRepository.findAllById(quoteFeedIds).stream().collect(Collectors.toMap(Feed::getId, f -> f));
        Set<Long> quoteAuthorIds = quoteFeedMap.values().stream().map(Feed::getUserId).collect(Collectors.toSet());
        Map<Long, User> quoteUserMap = quoteAuthorIds.isEmpty() ? Map.of()
                : userRepository.findByIdIn(new ArrayList<>(quoteAuthorIds)).stream().collect(Collectors.toMap(User::getId, u -> u));

        List<FeedItemResponse> result = new ArrayList<>();
        for (Feed feed : feeds) {
            User author = userMap.get(feed.getUserId());
            FeedUserResponse userResponse = toFeedUserResponse(author);

            FeedContentResponse contentResponse = FeedContentResponse.builder()
                    .text(feed.getText())
                    .contentImgs(feed.getContentImgs() != null ? feed.getContentImgs() : List.of())
                    .vendor(feed.getVendor())
                    .productUrl(feed.getProductUrl())
                    .title(feed.getTitle())
                    .rating(feed.getRating())
                    .quoteContent(feed.getQuoteFeedId() != null ? buildQuoteContentResponse(quoteFeedMap.get(feed.getQuoteFeedId()), quoteUserMap) : null)
                    .build();

            FeedCountsResponse countsResponse = FeedCountsResponse.builder()
                    .commentCount(feed.getCommentCount())
                    .likeCount(feed.getLikeCount())
                    .dislikeCount(feed.getDislikeCount())
                    .quoteCount(feed.getQuoteCount())
                    .build();

            FeedReaction reaction = reactionMap.get(feed.getId());
            ReactionType reactionType = reaction != null ? reaction.getReactionType() : ReactionType.NONE;
            FeedMyStateResponse myState = FeedMyStateResponse.builder()
                    .reaction(reactionType)
                    .isBookmarked(bookmarkedFeedIds.contains(feed.getId()))
                    .isReposted(repostedFeedIds.contains(feed.getId()))
                    .build();

            result.add(FeedItemResponse.builder()
                    .feedId(feed.getId())
                    .contentType(feed.getContentType())
                    .user(userResponse)
                    .content(contentResponse)
                    .counts(countsResponse)
                    .myState(myState)
                    .build());
        }
        return result;
    }

    private FeedUserResponse toFeedUserResponse(User user) {
        if (user == null) {
            return FeedUserResponse.builder()
                    .userId("")
                    .name("")
                    .profileImageUrl(null)
                    .build();
        }
        return FeedUserResponse.builder()
                .profileImageUrl(user.getProfileImageUrl())
                .name(user.getNickname() != null ? user.getNickname() : user.getName())
                .userId(user.getLoginId())
                .build();
    }

    private FeedQuoteContentResponse buildQuoteContentResponse(Feed quoteFeed, Map<Long, User> userMap) {
        if (quoteFeed == null) return null;
        User quoteUser = userMap.get(quoteFeed.getUserId());
        return FeedQuoteContentResponse.builder()
                .user(toFeedUserResponse(quoteUser))
                .vendor(quoteFeed.getVendor())
                .title(quoteFeed.getTitle())
                .rating(quoteFeed.getRating())
                .text(quoteFeed.getText())
                .contentImgs(quoteFeed.getContentImgs() != null ? quoteFeed.getContentImgs() : List.of())
                .build();
    }
}

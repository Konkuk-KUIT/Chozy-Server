package com.kuit.chozy.community.service;

import com.kuit.chozy.community.dto.request.CommentCreateRequest;
import com.kuit.chozy.community.dto.response.*;
import com.kuit.chozy.community.domain.*;
import com.kuit.chozy.community.repository.FeedBookmarkRepository;
import com.kuit.chozy.community.repository.FeedCommentReactionRepository;
import com.kuit.chozy.community.repository.FeedCommentRepository;
import com.kuit.chozy.community.repository.FeedImageRepository;
import com.kuit.chozy.community.repository.FeedReactionRepository;
import com.kuit.chozy.community.repository.FeedRepostRepository;
import com.kuit.chozy.community.repository.FeedRepository;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.dto.FollowStatus;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import com.kuit.chozy.userrelation.domain.FollowRequestStatus;
import com.kuit.chozy.userrelation.repository.MuteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityFeedService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final FeedRepository feedRepository;
    private final FeedReactionRepository feedReactionRepository;
    private final FeedRepostRepository feedRepostRepository;
    private final FeedBookmarkRepository feedBookmarkRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final FeedCommentReactionRepository feedCommentReactionRepository;
    private final FeedImageRepository feedImageRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final BlockRepository blockRepository;
    private final MuteRepository muteRepository;

    public FeedListResultResponse getFeeds(
            Long userId,
            FeedTab tab,
            String contentTypeParam,
            String search,
            String cursor,
            int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        if (size <= 0) safeSize = DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(0, safeSize + 1);

        Long cursorId = decodeCursor(cursor);
        FeedContentType contentType = parseContentType(contentTypeParam);
        List<Feed> feeds;

        if (StringUtils.hasText(search)) {
            feeds = getFeedsBySearchCursor(search.trim(), tab, contentType, userId, cursorId, pageable);
        } else {
            feeds = getFeedsWithoutSearchCursor(tab, contentType, userId, cursorId, pageable);
        }

        boolean hasNext = feeds.size() > safeSize;
        if (hasNext) {
            feeds = feeds.subList(0, safeSize);
        }
        if (feeds.isEmpty()) {
            return FeedListResultResponse.builder()
                    .feeds(List.of())
                    .hasNext(false)
                    .nextCursor(null)
                    .build();
        }

        List<FeedItemResponse> items = buildFeedItemResponses(feeds, userId);
        String nextCursor = hasNext ? encodeCursor(feeds.get(feeds.size() - 1).getId()) : null;
        return FeedListResultResponse.builder()
                .feeds(items)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .build();
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Long.parseLong(decoded.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String encodeCursor(Long feedId) {
        if (feedId == null) return null;
        return Base64.getEncoder().encodeToString(String.valueOf(feedId).getBytes(StandardCharsets.UTF_8));
    }

    private FeedContentType parseContentType(String contentTypeParam) {
        if (contentTypeParam == null || "ALL".equalsIgnoreCase(contentTypeParam)) {
            return null;
        }
        return FeedContentType.valueOf(contentTypeParam.toUpperCase());
    }

    private List<Feed> getFeedsWithoutSearchCursor(FeedTab tab, FeedContentType contentType, Long userId, Long cursorId, Pageable pageable) {
        if (tab == FeedTab.FOLLOWING) {
            if (userId == null) {
                return List.of();
            }
            List<Long> followingUserIds = followRepository.findByFollowerId(userId, Pageable.unpaged())
                    .getContent().stream()
                    .map(f -> f.getFollowingId())
                    .toList();
            if (followingUserIds.isEmpty()) return List.of();

            List<Long> mutedIds = muteRepository.findMutedIdsByMuterIdAndActiveTrue(userId);
            List<Long> blockedIds = blockRepository.findBlockedIdsByBlockerIdAndActiveTrue(userId);
            List<Long> allowedFollowing = followingUserIds.stream()
                    .filter(id -> !mutedIds.contains(id) && !blockedIds.contains(id))
                    .toList();
            if (allowedFollowing.isEmpty()) return List.of();

            List<Long> blockerIds = blockRepository.findBlockerIdsByBlockedIdAndActiveTrue(userId);
            Set<Long> blockExcludeSet = new HashSet<>(blockerIds);
            blockExcludeSet.addAll(blockedIds);
            if (blockExcludeSet.isEmpty()) {
                return feedRepository.findForFollowingCursor(allowedFollowing, cursorId, contentType, pageable);
            }
            List<Long> blockExcludeList = new ArrayList<>(blockExcludeSet);
            return feedRepository.findForFollowingCursorExcluding(allowedFollowing, blockExcludeList, cursorId, contentType, pageable);
        }

        // RECOMMEND: 로그인 시 나를 차단한 사람 + 내가 차단한 사람 + 내가 관심없음한 사람 게시물 제외
        if (userId != null) {
            List<Long> blockerIds = blockRepository.findBlockerIdsByBlockedIdAndActiveTrue(userId);
            List<Long> blockedIds = blockRepository.findBlockedIdsByBlockerIdAndActiveTrue(userId);
            List<Long> mutedIds = muteRepository.findMutedIdsByMuterIdAndActiveTrue(userId);
            Set<Long> excludeSet = new HashSet<>(blockerIds);
            excludeSet.addAll(blockedIds);
            excludeSet.addAll(mutedIds);
            if (!excludeSet.isEmpty()) {
                List<Long> excludeUserIds = new ArrayList<>(excludeSet);
                return feedRepository.findForRecommendCursorExcluding(cursorId, contentType, excludeUserIds, pageable);
            }
        }
        return feedRepository.findForRecommendCursor(cursorId, contentType, pageable);
    }

    private List<Feed> getFeedsBySearchCursor(String search, FeedTab tab, FeedContentType contentType, Long userId, Long cursorId, Pageable pageable) {
        if (tab == FeedTab.FOLLOWING) {
            if (userId == null) {
                return List.of();
            }
            List<Long> followingUserIds = followRepository.findByFollowerId(userId, Pageable.unpaged())
                    .getContent().stream()
                    .map(f -> f.getFollowingId())
                    .toList();
            if (followingUserIds.isEmpty()) return List.of();

            List<Long> mutedIds = muteRepository.findMutedIdsByMuterIdAndActiveTrue(userId);
            List<Long> blockedIds = blockRepository.findBlockedIdsByBlockerIdAndActiveTrue(userId);
            List<Long> allowedFollowing = followingUserIds.stream()
                    .filter(id -> !mutedIds.contains(id) && !blockedIds.contains(id))
                    .toList();
            if (allowedFollowing.isEmpty()) return List.of();

            List<Long> blockerIds = blockRepository.findBlockerIdsByBlockedIdAndActiveTrue(userId);
            Set<Long> blockExcludeSet = new HashSet<>(blockerIds);
            blockExcludeSet.addAll(blockedIds);
            if (blockExcludeSet.isEmpty()) {
                return feedRepository.findForFollowingCursorWithSearch(allowedFollowing, cursorId, contentType, search, pageable);
            }
            List<Long> blockExcludeList = new ArrayList<>(blockExcludeSet);
            return feedRepository.findForFollowingCursorWithSearchExcluding(allowedFollowing, blockExcludeList, cursorId, contentType, search, pageable);
        }

        if (userId != null) {
            List<Long> blockerIds = blockRepository.findBlockerIdsByBlockedIdAndActiveTrue(userId);
            List<Long> blockedIds = blockRepository.findBlockedIdsByBlockerIdAndActiveTrue(userId);
            List<Long> mutedIds = muteRepository.findMutedIdsByMuterIdAndActiveTrue(userId);
            Set<Long> excludeSet = new HashSet<>(blockerIds);
            excludeSet.addAll(blockedIds);
            excludeSet.addAll(mutedIds);
            if (!excludeSet.isEmpty()) {
                List<Long> excludeUserIds = new ArrayList<>(excludeSet);
                return feedRepository.findForRecommendCursorWithSearchExcluding(cursorId, contentType, search, excludeUserIds, pageable);
            }
        }
        return feedRepository.findForRecommendCursorWithSearch(cursorId, contentType, search, pageable);
    }

    private List<FeedItemResponse> buildFeedItemResponses(List<Feed> feeds, Long currentUserId) {
        List<Long> feedIds = feeds.stream().map(Feed::getId).toList();
        Set<Long> authorIds = feeds.stream().map(Feed::getUserId).collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findByIdIn(new ArrayList<>(authorIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, FeedReaction> reactionMap =
                (currentUserId == null)
                        ? Map.of()
                        : feedReactionRepository.findByUserIdAndFeedIdIn(currentUserId, feedIds).stream()
                        .collect(Collectors.toMap(FeedReaction::getFeedId, r -> r));

        Set<Long> repostedFeedIds =
                (currentUserId == null)
                        ? Set.of()
                        : feedRepostRepository.findByUserIdAndSourceFeedIdIn(currentUserId, feedIds).stream()
                        .map(FeedRepost::getSourceFeedId)
                        .collect(Collectors.toSet());

        Set<Long> bookmarkedFeedIds =
                (currentUserId == null)
                        ? Set.of()
                        : feedBookmarkRepository.findByUserIdAndFeedIdIn(currentUserId, feedIds).stream()
                        .map(FeedBookmark::getFeedId)
                        .collect(Collectors.toSet());

        Map<Long, List<FeedImage>> feedImagesMap = feedImageRepository.findByFeed_IdIn(feedIds).stream()
                .collect(Collectors.groupingBy(fi -> fi.getFeed().getId()));

        // QUOTE, REPOST 모두 원문(OriginalFeed) 정보 필요 → contents.quote에 채움
        Set<Long> quoteOriginalIds = feeds.stream()
                .filter(f -> (f.getKind() == FeedKind.QUOTE || f.getKind() == FeedKind.REPOST) && f.getOriginalFeedId() != null)
                .map(Feed::getOriginalFeedId)
                .collect(Collectors.toSet());

        Map<Long, Feed> quoteOriginalFeedMap = quoteOriginalIds.isEmpty()
                ? Map.of()
                : feedRepository.findAllById(quoteOriginalIds).stream()
                .collect(Collectors.toMap(Feed::getId, f -> f));

        Set<Long> quoteOriginalAuthorIds = quoteOriginalFeedMap.values().stream()
                .map(Feed::getUserId)
                .collect(Collectors.toSet());

        Map<Long, User> quoteOriginalUserMap = quoteOriginalAuthorIds.isEmpty()
                ? Map.of()
                : userRepository.findByIdIn(new ArrayList<>(quoteOriginalAuthorIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<FeedItemResponse> result = new ArrayList<>();
        for (Feed feed : feeds) {
            User author = userMap.get(feed.getUserId());
            FeedUserResponse userResponse = toFeedUserResponse(author);

            List<FeedImage> images = feedImagesMap.getOrDefault(feed.getId(), List.of());
            List<FeedImageItemResponse> imageItems = images.stream()
                    .sorted(Comparator.comparing(FeedImage::getSortOrder))
                    .map(img -> FeedImageItemResponse.builder()
                            .imageUrl(img.getImageUrl())
                            .sortOrder(img.getSortOrder() != null ? img.getSortOrder() : 0)
                            .contentType(img.getContentType() != null ? img.getContentType() : "image/jpeg")
                            .build())
                    .toList();

            FeedReviewInContentResponse reviewContent = null;
            if (feed.getContentType() == FeedContentType.REVIEW) {
                reviewContent = FeedReviewInContentResponse.builder()
                        .vendor(feed.getVendor())
                        .title(feed.getTitle())
                        .rating(feed.getRating())
                        .productUrl(feed.getProductUrl())
                        .build();
            }

            // kind=QUOTE 또는 REPOST일 때 원문 정보. REPOST는 최상위 text 없음, QUOTE는 사용자 추가 text 있음(resolveFeedText에서 처리)
            FeedQuoteInContentResponse quoteContent = null;
            if ((feed.getKind() == FeedKind.QUOTE || feed.getKind() == FeedKind.REPOST) && feed.getOriginalFeedId() != null) {
                Feed original = quoteOriginalFeedMap.get(feed.getOriginalFeedId());
                if (original != null) {
                    User ou = quoteOriginalUserMap.get(original.getUserId());
                    quoteContent = FeedQuoteInContentResponse.builder()
                            .feedId(original.getId())
                            .user(toFeedUserResponse(ou))
                            .text(resolveOriginalText(original))
                            .hashTags(parseHashtags(original.getHashtags()))
                            .build();
                }
            }

            FeedListContentResponse contents = FeedListContentResponse.builder()
                    .text(resolveFeedText(feed))
                    .images(imageItems)
                    .review(reviewContent)
                    .quote(quoteContent)
                    .build();

            FeedCountsResponse countsResponse = FeedCountsResponse.builder()
                    .viewCount(feed.getViewCount() != null ? feed.getViewCount() : 0)
                    .commentCount(feed.getCommentCount())
                    .likeCount(feed.getLikeCount())
                    .dislikeCount(feed.getDislikeCount())
                    .quoteCount(feed.getShareCount())
                    .build();

            FeedReaction reaction = reactionMap.get(feed.getId());
            ReactionType reactionType = reaction != null ? reaction.getReactionType() : ReactionType.NONE;

            boolean isFollowing = false;
            FollowStatus followStatus = FollowStatus.NONE;
            if (currentUserId != null && !currentUserId.equals(feed.getUserId())) {
                isFollowing = followRepository.existsByFollowerIdAndFollowingIdAndStatus(currentUserId, feed.getUserId(), FollowStatus.FOLLOWING);
                followStatus = isFollowing ? FollowStatus.FOLLOWING : FollowStatus.NONE;
            }

            FeedMyStateResponse myState = FeedMyStateResponse.builder()
                    .reactionType(reactionType)
                    .isBookmarked(bookmarkedFeedIds.contains(feed.getId()))
                    .isReposted(repostedFeedIds.contains(feed.getId()))
                    .isFollowing(isFollowing)
                    .followStatus(followStatus)
                    .build();

            result.add(FeedItemResponse.builder()
                    .feedId(feed.getId())
                    .kind(feed.getKind())
                    .contentType(feed.getContentType())
                    .isMine(feed.getUserId().equals(currentUserId))
                    .createdAt(feed.getCreatedAt())
                    .user(userResponse)
                    .contents(contents)
                    .counts(countsResponse)
                    .myState(myState)
                    .build());
        }
        return result;
    }

    /**
     * Feed 리스트를 FeedItemResponse 리스트로 변환 (마이피드 등에서 재사용)
     */
    public List<FeedItemResponse> toFeedItemResponses(List<Feed> feeds, Long currentUserId) {
        if (feeds == null || feeds.isEmpty()) return List.of();
        return buildFeedItemResponses(feeds, currentUserId);
    }

    private List<String> parseHashtags(String hashtags) {
        if (hashtags == null || hashtags.isBlank()) return List.of();
        try {
            if (hashtags.startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(hashtags, mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            }
            return List.of();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String toHashtagsJson(List<String> tags) {
        if (tags == null || tags.isEmpty()) return "[]";

        List<String> normalized = tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(normalized);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String resolveFeedText(Feed feed) {
        if (feed == null) return null;

        if (feed.getKind() == FeedKind.QUOTE) {
            return feed.getQuoteText();
        }
        return feed.getContent();
    }

    private String resolveOriginalText(Feed original) {
        if (original == null) return null;

        if (original.getKind() == FeedKind.QUOTE) {
            return original.getQuoteText();
        }
        return original.getContent();
    }

    private FeedUserResponse toFeedUserResponse(User user) {
        if (user == null) {
            return FeedUserResponse.builder()
                    .userPk(null)
                    .userId("")
                    .name("")
                    .profileImageUrl(null)
                    .build();
        }
        return FeedUserResponse.builder()
                .userPk(user.getId())
                .profileImageUrl(user.getProfileImageUrl())
                .name(user.getNickname() != null ? user.getNickname() : user.getName())
                .userId(user.getLoginId())
                .build();
    }

    // ========== 게시글 상세 / 반응 / 북마크 / 삭제 / 댓글 ==========

    @Transactional(readOnly = false)
    public FeedDetailResponse getFeedDetail(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        Integer vc = Math.toIntExact(feed.getViewCount() == null ? 0 : feed.getViewCount());
        feed.setViewCount(vc + 1);
        feedRepository.save(feed);

        User author = userRepository.findById(feed.getUserId()).orElse(null);
        boolean isMine = feed.getUserId().equals(userId);

        FeedUserResponse userResponse = toFeedUserResponse(author);

        List<FeedImage> feedImages = feedImageRepository.findByFeed_Id(feedId).stream()
                .sorted(Comparator.comparing(FeedImage::getSortOrder))
                .toList();
        List<FeedImageItemResponse> feedImageItems = feedImages.stream()
                .map(img -> FeedImageItemResponse.builder()
                        .imageUrl(img.getImageUrl())
                        .sortOrder(img.getSortOrder() != null ? img.getSortOrder() : 0)
                        .contentType(img.getContentType() != null ? img.getContentType() : "image/jpeg")
                        .build())
                .toList();

        FeedQuoteInContentResponse quoteContent = null;
        if ((feed.getKind() == FeedKind.QUOTE || feed.getKind() == FeedKind.REPOST) && feed.getOriginalFeedId() != null) {
            Feed original = feedRepository.findById(feed.getOriginalFeedId()).orElse(null);
            if (original != null) {
                User ou = userRepository.findById(original.getUserId()).orElse(null);
                quoteContent = FeedQuoteInContentResponse.builder()
                        .feedId(original.getId())
                        .user(toFeedUserResponse(ou))
                        .text(resolveOriginalText(original))
                        .hashTags(parseHashtags(original.getHashtags()))
                        .build();
            }
        }

        FeedDetailContentResponse contents = FeedDetailContentResponse.builder()
                .vendor(feed.getVendor())
                .productUrl(feed.getProductUrl())
                .title(feed.getTitle())
                .rating(feed.getRating())
                .content(resolveFeedText(feed))
                .feedImages(feedImageItems)
                .hashTags(parseHashtags(feed.getHashtags()))
                .quote(quoteContent)
                .build();

        FeedCountsResponse countsResponse = FeedCountsResponse.builder()
                .viewCount(feed.getViewCount() != null ? feed.getViewCount() : 0)
                .commentCount(feed.getCommentCount())
                .likeCount(feed.getLikeCount())
                .dislikeCount(feed.getDislikeCount())
                .quoteCount(feed.getShareCount())
                .build();

        FeedReaction feedReaction = null;
        boolean isBookmarked = false;
        boolean isReposted = false;
        boolean isFollowing = false;
        FollowStatus followStatus = FollowStatus.NONE;

        if (userId != null) {
            feedReaction = feedReactionRepository.findByUserIdAndFeedId(userId, feedId).orElse(null);

            isBookmarked = feedBookmarkRepository.findByUserIdAndFeedIdIn(userId, List.of(feedId)).stream()
                    .anyMatch(b -> b.getFeedId().equals(feedId));

            isReposted = feedRepostRepository.existsByUserIdAndSourceFeedId(userId, feedId);

            if (!userId.equals(feed.getUserId())) {
                isFollowing = followRepository.existsByFollowerIdAndFollowingIdAndStatus(userId, feed.getUserId(), FollowStatus.FOLLOWING);
                boolean isRequested = followRequestRepository.existsByRequesterIdAndTargetIdAndStatus(userId, feed.getUserId(), FollowRequestStatus.PENDING);
                followStatus = isFollowing ? FollowStatus.FOLLOWING : (isRequested ? FollowStatus.REQUESTED : FollowStatus.NONE);
            }
        }

        FeedMyStateResponse myState = FeedMyStateResponse.builder()
                .reactionType(feedReaction != null ? feedReaction.getReactionType() : ReactionType.NONE)
                .isBookmarked(isBookmarked)
                .isReposted(isReposted)
                .isFollowing(isFollowing)
                .followStatus(followStatus)
                .build();

        FeedDetailFeedResponse feedDetail = FeedDetailFeedResponse.builder()
                .feedId(feed.getId())
                .kind(feed.getKind())
                .contentType(feed.getContentType())
                .isMine(isMine)
                .createdAt(feed.getCreatedAt())
                .user(userResponse)
                .contents(contents)
                .counts(countsResponse)
                .myState(myState)
                .build();

        // 댓글 전체를 한 번에 조회한 뒤, parentCommentId 기반으로 트리 구조를 빌드
        List<FeedComment> allComments = feedCommentRepository.findByFeedId(feedId);
        List<CommentItemResponse> comments = buildCommentTree(allComments, userId);

        return FeedDetailResponse.builder()
                .feed(feedDetail)
                .comments(comments)
                .build();
    }

    /**
     * 댓글 전체 리스트를 parentCommentId 기반으로 트리 구조로 변환한다.
     * depth: 0(최상위), 1(대댓글), 2(대댓글의 대댓글) ...
     */
    private List<CommentItemResponse> buildCommentTree(List<FeedComment> comments, Long currentUserId) {
        if (comments.isEmpty()) return List.of();

        // commentId, authorIds, replyToUserIds 수집
        List<Long> commentIds = comments.stream()
                .map(FeedComment::getId)
                .toList();

        Set<Long> authorIds = comments.stream()
                .map(FeedComment::getUserId)
                .collect(Collectors.toSet());

        Set<Long> replyToUserIds = comments.stream()
                .map(FeedComment::getReplyToUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // authorIds ∪ replyToUserIds 에 해당하는 사용자들 한번에 조회
        Set<Long> allUserIds = new HashSet<>();
        allUserIds.addAll(authorIds);
        allUserIds.addAll(replyToUserIds);

        Map<Long, User> userMap = allUserIds.isEmpty()
                ? Map.of()
                : userRepository.findByIdIn(new ArrayList<>(allUserIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 현재 유저의 댓글 반응(좋아요/싫어요) 맵
        Map<Long, FeedCommentReaction> reactionMap =
                (currentUserId == null)
                        ? Map.of()
                        : feedCommentReactionRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
                        .collect(Collectors.toMap(FeedCommentReaction::getCommentId, r -> r));

        // parentCommentId → 자식 댓글 목록 매핑
        Map<Long, List<FeedComment>> childrenByParentId = comments.stream()
                .filter(c -> c.getParentCommentId() != null)
                .collect(Collectors.groupingBy(FeedComment::getParentCommentId));

        // 최상위 댓글(부모가 없는 댓글)만 root로 삼아 트리 구성
        List<FeedComment> topLevel = comments.stream()
                .filter(c -> c.getParentCommentId() == null)
                .sorted(Comparator.comparing(FeedComment::getCreatedAt))
                .toList();

        List<CommentItemResponse> result = new ArrayList<>();
        for (FeedComment root : topLevel) {
            result.add(buildCommentNode(
                    root,
                    0,
                    currentUserId,
                    userMap,
                    reactionMap,
                    childrenByParentId
            ));
        }
        return result;
    }

    private CommentItemResponse buildCommentNode(
            FeedComment comment,
            int depth,
            Long currentUserId,
            Map<Long, User> userMap,
            Map<Long, FeedCommentReaction> reactionMap,
            Map<Long, List<FeedComment>> childrenByParentId
    ) {
        User author = userMap.get(comment.getUserId());
        FeedCommentReaction reaction = reactionMap.get(comment.getId());

        CommentReplyToResponse replyTo = null;
        if (comment.getReplyToUserId() != null) {
            User replyToUser = userMap.get(comment.getReplyToUserId());
            replyTo = toCommentReplyTo(replyToUser);
        }

        // 자식 댓글들 재귀적으로 빌드
        List<FeedComment> children = childrenByParentId.getOrDefault(comment.getId(), List.of());
        List<CommentItemResponse> childResponses = children.stream()
                .sorted(Comparator.comparing(FeedComment::getCreatedAt))
                .map(child -> buildCommentNode(
                        child,
                        depth + 1,
                        currentUserId,
                        userMap,
                        reactionMap,
                        childrenByParentId
                ))
                .toList();

        return CommentItemResponse.builder()
                .commentId(comment.getId())
                .parentCommentId(comment.getParentCommentId())
                .depth(depth)
                .isMine(currentUserId != null && comment.getUserId().equals(currentUserId))
                .user(toFeedUserResponse(author))
                .content(comment.getContent())
                .replyTo(replyTo)
                .mentions(List.of())
                .counts(CommentCountsResponse.builder()
                        .likeCount(comment.getLikeCount())
                        .dislikeCount(comment.getDislikeCount())
                        .replyCount(comment.getCommentCount())
                        .build())
                .myState(CommentMyStateResponse.builder()
                        .reactionType(reaction != null ? reaction.getReactionType() : ReactionType.NONE)
                        .isBookmarked(false)
                        .isReposted(false)
                        .isFollowing(false)
                        .build())
                .status(comment.getStatus() != null ? comment.getStatus() : CommentStatus.ACTIVE)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt() != null ? comment.getUpdatedAt() : comment.getCreatedAt())
                .replies(childResponses)
                .hasMoreReplies(false)
                .nextRepliesCursor(null)
                .build();
    }

    private CommentReplyToResponse toCommentReplyTo(User user) {
        if (user == null) return null;
        return CommentReplyToResponse.builder()
                .userId(user.getLoginId())
                .name(user.getNickname() != null ? user.getNickname() : user.getName())
                .build();
    }

    @Transactional(readOnly = false)
    public void setFeedReaction(Long feedId, Long userId, boolean like) {
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));
        ReactionType type = like ? ReactionType.LIKE : ReactionType.DISLIKE;

        Optional<FeedReaction> existing = feedReactionRepository.findByUserIdAndFeedId(userId, feedId);
        FeedReaction reaction = existing.orElse(FeedReaction.builder().userId(userId).feedId(feedId).reactionType(type).build());

        if (existing.isPresent()) {
            ReactionType old = reaction.getReactionType();
            if (old == ReactionType.LIKE) feed.setLikeCount((int) Math.max(0, feed.getLikeCount() - 1));
            else feed.setDislikeCount((int) Math.max(0, feed.getDislikeCount() - 1));
        }

        reaction.setReactionType(type);

        if (type == ReactionType.LIKE) feed.setLikeCount((int) (feed.getLikeCount() + 1));
        else feed.setDislikeCount((int) (feed.getDislikeCount() + 1));

        feedReactionRepository.save(reaction);
        feedRepository.save(feed);
    }

    @Transactional(readOnly = false)
    public void setFeedBookmark(Long feedId, Long userId, boolean bookmark) {
        feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        Optional<FeedBookmark> existing = feedBookmarkRepository.findByUserIdAndFeedIdIn(userId, List.of(feedId)).stream().findFirst();
        if (bookmark && existing.isEmpty()) {
            feedBookmarkRepository.save(FeedBookmark.builder().userId(userId).feedId(feedId).build());
        } else if (!bookmark && existing.isPresent()) {
            feedBookmarkRepository.delete(existing.get());
        }
    }

    @Transactional(readOnly = false)
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FEED_DELETE_FORBIDDEN);
        }

        if (feed.getKind() == FeedKind.REPOST || feed.getKind() == FeedKind.QUOTE) {
            Long originalId = feed.getOriginalFeedId();

            if (originalId != null) {
                feedRepository.findById(originalId).ifPresent(original -> {
                    int current = (original.getShareCount() == null ? 0 : original.getShareCount());
                    original.setShareCount(Math.max(0, current - 1));
                    feedRepository.save(original);
                });
            }
        }

        List<FeedComment> comments = feedCommentRepository.findByFeedId(feedId);
        List<Long> commentIds = comments.stream().map(FeedComment::getId).toList();

        if (!commentIds.isEmpty()) {
            feedCommentReactionRepository.findByCommentIdIn(commentIds)
                    .forEach(feedCommentReactionRepository::delete);
        }

        feedCommentRepository.deleteAll(comments);

        feedReactionRepository.findByFeedId(feedId)
                .forEach(feedReactionRepository::delete);

        feedBookmarkRepository.findByFeedId(feedId)
                .forEach(feedBookmarkRepository::delete);

        feedRepostRepository.findBySourceFeedIdOrTargetFeedId(feedId)
                .forEach(feedRepostRepository::delete);

        feedRepository.delete(feed);
    }

    @Transactional(readOnly = false)
    public CommentCreateResponse createComment(Long feedId, Long userId, CommentCreateRequest request) {
        feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));
        if (request.getContent() == null || request.getContent().isBlank()) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        Long replyToUserId = null;
        if (request.getReplyToUserId() != null && !request.getReplyToUserId().isBlank()) {
            replyToUserId = userRepository.findByLoginId(request.getReplyToUserId()).map(User::getId).orElse(null);
        }

        Long parentId = request.getParentCommentId();
        if (parentId != null && parentId == 0L) {
            parentId = null;
        }

        FeedComment comment = FeedComment.builder()
                .feedId(feedId)
                .userId(userId)
                .parentCommentId(parentId)
                .content(request.getContent().trim())
                .mentionName(null)
                .replyToUserId(replyToUserId)
                .build();

        comment = feedCommentRepository.save(comment);

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));
        feed.setCommentCount((int) (feed.getCommentCount() + 1));
        feedRepository.save(feed);

        if (parentId != null) {
            feedCommentRepository.findById(parentId).ifPresent(parent -> {
                parent.setCommentCount(parent.getCommentCount() + 1);
                feedCommentRepository.save(parent);
            });
        }

        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .feedId(feedId)
                .parentCommentId(parentId)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = false)
    public void setCommentReaction(Long commentId, Long userId, boolean like) {
        FeedComment comment = feedCommentRepository.findById(commentId).orElseThrow(() -> new ApiException(ErrorCode.COMMENT_NOT_FOUND));
        ReactionType type = like ? ReactionType.LIKE : ReactionType.DISLIKE;

        Optional<FeedCommentReaction> existing = feedCommentReactionRepository.findByUserIdAndCommentId(userId, commentId);
        FeedCommentReaction reaction = existing.orElse(FeedCommentReaction.builder().userId(userId).commentId(commentId).reactionType(type).build());

        if (existing.isPresent()) {
            ReactionType old = reaction.getReactionType();
            if (old == ReactionType.LIKE) comment.setLikeCount(Math.max(0, comment.getLikeCount() - 1));
            else comment.setDislikeCount(Math.max(0, comment.getDislikeCount() - 1));
        }

        reaction.setReactionType(type);

        if (type == ReactionType.LIKE) comment.setLikeCount(comment.getLikeCount() + 1);
        else comment.setDislikeCount(comment.getDislikeCount() + 1);

        feedCommentReactionRepository.save(reaction);
        feedCommentRepository.save(comment);
    }

    @Transactional
    public Long createPostFeed(Long userId, String content, List<String> imageUrls, List<String> hashTags) {
        if (!StringUtils.hasText(content)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        Feed feed = Feed.builder()
                .userId(userId)
                .kind(FeedKind.ORIGINAL)
                .contentType(FeedContentType.POST)
                .content(content.trim())
                .hashtags(toHashtagsJson(hashTags))
                .build();

        Feed saved = feedRepository.save(feed);

        return saved.getId();
    }

    @Transactional
    public Long createReviewFeed(
            Long userId,
            String title,
            String content,
            String vendor,
            Float rating,
            String productUrl,
            List<String> imageUrls,
            List<String> hashTags
    ) {
        if (!StringUtils.hasText(title)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        if (!StringUtils.hasText(content)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        if (!StringUtils.hasText(vendor)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        Feed feed = Feed.builder()
                .userId(userId)
                .kind(FeedKind.ORIGINAL)
                .contentType(FeedContentType.REVIEW)
                .title(title.trim())
                .content(content.trim())
                .vendor(vendor.trim())
                .productUrl(productUrl)
                .rating(rating == null ? null : new java.math.BigDecimal(String.valueOf(rating)))
                .hashtags(toHashtagsJson(hashTags))
                .build();

        return feedRepository.save(feed).getId();
    }


    @Transactional
    public Long createRepost(Long userId, Long sourceFeedId) {
        Feed source = feedRepository.findById(sourceFeedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        if (feedRepository.existsByUserIdAndKindAndOriginalFeedId(userId, FeedKind.QUOTE, sourceFeedId)) {
            throw new ApiException(ErrorCode.CANNOT_REPOST_WHEN_QUOTED);
        }

        if (feedRepostRepository.existsByUserIdAndSourceFeedId(userId, sourceFeedId)) {
            throw new ApiException(ErrorCode.REPOST_ALREADY_EXISTS);
        }

        Feed repostFeed = Feed.builder()
                .userId(userId)
                .kind(FeedKind.REPOST)
                .contentType(FeedContentType.POST)
                .originalFeedId(sourceFeedId)
                .content(null)
                .hashtags("[]")
                .status(FeedStatus.ACTIVE)
                .build();

        Feed saved = feedRepository.save(repostFeed);

        feedRepostRepository.save(
                FeedRepost.builder()
                        .userId(userId)
                        .sourceFeedId(sourceFeedId)
                        .targetFeedId(saved.getId())
                        .build()
        );

        source.setShareCount((source.getShareCount() == null ? 0 : source.getShareCount()) + 1);
        feedRepository.save(source);

        return saved.getId();
    }

    @Transactional
    public Long createQuote(Long userId, Long sourceFeedId, String quoteText) {
        Feed source = feedRepository.findById(sourceFeedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        if (!StringUtils.hasText(quoteText)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (feedRepostRepository.existsByUserIdAndSourceFeedId(userId, sourceFeedId)) {
            throw new ApiException(ErrorCode.CANNOT_QUOTE_WHEN_REPOSTED);
        }

        if (feedRepository.existsByUserIdAndKindAndOriginalFeedId(userId, FeedKind.QUOTE, sourceFeedId)) {
            throw new ApiException(ErrorCode.QUOTE_ALREADY_EXISTS);
        }

        Feed quoteFeed = Feed.builder()
                .userId(userId)
                .kind(FeedKind.QUOTE)
                .contentType(FeedContentType.POST)
                .originalFeedId(sourceFeedId)
                .quoteText(quoteText.trim())
                .content(null)
                .hashtags("[]")
                .status(FeedStatus.ACTIVE)
                .build();

        Long newId = feedRepository.save(quoteFeed).getId();

        source.setShareCount((source.getShareCount() == null ? 0 : source.getShareCount()) + 1);
        feedRepository.save(source);

        return newId;
    }

    @Transactional
    public void updatePostFeed(Long feedId, Long userId, String content, List<String> hashTags, List<String> imageUrls) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getUserId().equals(userId)) throw new ApiException(ErrorCode.FEED_UPDATE_FORBIDDEN);
        if (feed.getKind() == FeedKind.REPOST) throw new ApiException(ErrorCode.FEED_UPDATE_FORBIDDEN);

        if (feed.getContentType() != FeedContentType.POST) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        if (feed.getKind() == FeedKind.ORIGINAL) {
            if (!StringUtils.hasText(content)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            feed.setContent(content.trim());
        } else if (feed.getKind() == FeedKind.QUOTE) {
            if (!StringUtils.hasText(content)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            feed.setQuoteText(content.trim());
        }

        if (hashTags != null) {
            feed.setHashtags(toHashtagsJson(hashTags));
        }

        feedRepository.save(feed);
    }

    @Transactional
    public void updateReviewFeed(
            Long feedId,
            Long userId,
            String title,
            String content,
            String vendor,
            Float rating,
            String productUrl,
            List<String> hashTags,
            List<String> imageUrls
    ) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getUserId().equals(userId)) throw new ApiException(ErrorCode.FEED_UPDATE_FORBIDDEN);
        if (feed.getKind() != FeedKind.ORIGINAL) throw new ApiException(ErrorCode.FEED_UPDATE_FORBIDDEN);
        if (feed.getContentType() != FeedContentType.REVIEW) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        if (!StringUtils.hasText(title)
                || !StringUtils.hasText(content)
                || !StringUtils.hasText(vendor)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        feed.setTitle(title.trim());
        feed.setContent(content.trim());
        feed.setVendor(vendor.trim());
        feed.setProductUrl(productUrl);
        feed.setRating(rating == null ? null : new java.math.BigDecimal(String.valueOf(rating)));

        if (hashTags != null) feed.setHashtags(toHashtagsJson(hashTags));

        feedRepository.save(feed);
    }


    @Transactional
    public void cancelRepost(Long userId, Long sourceFeedId) {
        FeedRepost repost = feedRepostRepository.findByUserIdAndSourceFeedId(userId, sourceFeedId)
                .orElseThrow(() -> new ApiException(ErrorCode.REPOST_NOT_FOUND));

        Long targetFeedId = repost.getTargetFeedId();

        feedRepostRepository.delete(repost);

        if (targetFeedId != null) {
            feedRepository.findById(targetFeedId).ifPresent(feedRepository::delete);
        }

        Feed source = feedRepository.findById(sourceFeedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        int current = (source.getShareCount() == null ? 0 : source.getShareCount());
        source.setShareCount(Math.max(0, current - 1));
        feedRepository.save(source);
    }
}
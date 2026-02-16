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
    private final FeedCommentRepository feedCommentRepository;
    private final FeedCommentReactionRepository feedCommentReactionRepository;
    private final FeedImageRepository feedImageRepository;
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
                .map(FeedRepost::getSourceFeedId)
                .collect(Collectors.toSet());

        Set<Long> bookmarkedFeedIds = feedBookmarkRepository.findByUserIdAndFeedIdIn(currentUserId, feedIds).stream()
                .map(FeedBookmark::getFeedId)
                .collect(Collectors.toSet());

        // ===== 이미지: FeedImage 엔티티 기반 =====
        Map<Long, List<String>> feedImagesMap = feedImageRepository.findByFeed_IdIn(feedIds).stream()
                .collect(Collectors.groupingBy(
                        fi -> fi.getFeed().getId(),
                        Collectors.mapping(FeedImage::getImageUrl, Collectors.toList())
                ));

        // ===== 인용: QUOTE면 originalFeedId가 '인용 대상(원본)' =====
        Set<Long> quoteOriginalIds = feeds.stream()
                .filter(f -> f.getKind() == FeedKind.QUOTE && f.getOriginalFeedId() != null)
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

        Map<Long, List<String>> quoteOriginalImagesMap = quoteOriginalIds.isEmpty()
                ? Map.of()
                : feedImageRepository.findByFeed_IdIn(new ArrayList<>(quoteOriginalIds)).stream()
                .collect(Collectors.groupingBy(
                        fi -> fi.getFeed().getId(),
                        Collectors.mapping(FeedImage::getImageUrl, Collectors.toList())
                ));

        List<FeedItemResponse> result = new ArrayList<>();
        for (Feed feed : feeds) {
            User author = userMap.get(feed.getUserId());
            FeedUserResponse userResponse = toFeedUserResponse(author);

            List<String> contentImgs = feedImagesMap.getOrDefault(feed.getId(), List.of());

            FeedQuoteContentResponse quoteContent = null;
            if (feed.getKind() == FeedKind.QUOTE && feed.getOriginalFeedId() != null) {
                Feed original = quoteOriginalFeedMap.get(feed.getOriginalFeedId());
                quoteContent = buildQuoteContentResponse(
                        original,
                        quoteOriginalUserMap,
                        quoteOriginalImagesMap.getOrDefault(feed.getOriginalFeedId(), List.of())
                );
            }

            String text = resolveFeedText(feed);

            FeedContentResponse contentResponse = FeedContentResponse.builder()
                    .text(text)
                    .contentImgs(contentImgs)
                    .vendor(feed.getVendor())
                    .productUrl(feed.getProductUrl())
                    .title(null)
                    .rating(feed.getRating())
                    .quoteContent(quoteContent)
                    .build();

            FeedCountsResponse countsResponse = FeedCountsResponse.builder()
                    .views(null)
                    .commentCount(feed.getCommentCount())
                    .likeCount(feed.getLikeCount())
                    .dislikeCount(feed.getDislikeCount())
                    .quoteCount(feed.getShareCount())
                    .build();

            FeedReaction reaction = reactionMap.get(feed.getId());
            ReactionType reactionType = reaction != null ? reaction.getReactionType() : ReactionType.NONE;

            FeedMyStateResponse myState = FeedMyStateResponse.builder()
                    .reaction(reactionType)
                    .isBookmarked(bookmarkedFeedIds.contains(feed.getId()))
                    .isReposted(repostedFeedIds.contains(feed.getId()))
                    .isFollowing(null)
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

    private FeedQuoteContentResponse buildQuoteContentResponse(Feed originalFeed, Map<Long, User> userMap, List<String> originalImgs) {
        if (originalFeed == null) return null;
        User originalUser = userMap.get(originalFeed.getUserId());
        return FeedQuoteContentResponse.builder()
                .user(toFeedUserResponse(originalUser))
                .vendor(originalFeed.getVendor())
                .title(null)
                .rating(originalFeed.getRating())
                .text(resolveOriginalText(originalFeed))
                .contentImgs(originalImgs != null ? originalImgs : List.of())
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

        List<String> imgs = feedImageRepository.findByFeed_Id(feedId).stream()
                .map(FeedImage::getImageUrl)
                .toList();

        FeedQuoteContentResponse quoteContent = null;
        if (feed.getKind() == FeedKind.QUOTE && feed.getOriginalFeedId() != null) {
            Feed original = feedRepository.findById(feed.getOriginalFeedId()).orElse(null);
            if (original != null) {
                User ou = userRepository.findById(original.getUserId()).orElse(null);
                List<String> oimgs = feedImageRepository.findByFeed_Id(original.getId()).stream()
                        .map(FeedImage::getImageUrl)
                        .toList();
                quoteContent = FeedQuoteContentResponse.builder()
                        .user(toFeedUserResponse(ou))
                        .vendor(original.getVendor())
                        .title(null)
                        .rating(original.getRating())
                        .text(resolveOriginalText(original))
                        .contentImgs(oimgs)
                        .build();
            }
        }

        FeedContentResponse contentResponse = FeedContentResponse.builder()
                .text(resolveFeedText(feed))
                .contentImgs(imgs)
                .vendor(feed.getVendor())
                .productUrl(feed.getProductUrl())
                .title(null)
                .rating(feed.getRating())
                .quoteContent(quoteContent)
                .hashTags(feed.getHashtags())
                .build();

        FeedCountsResponse countsResponse = FeedCountsResponse.builder()
                .views(feed.getViewCount())
                .commentCount(feed.getCommentCount())
                .likeCount(feed.getLikeCount())
                .dislikeCount(feed.getDislikeCount())
                .quoteCount(feed.getShareCount())
                .build();

        FeedReaction feedReaction = feedReactionRepository.findByUserIdAndFeedId(userId, feedId).orElse(null);

        boolean isBookmarked = feedBookmarkRepository.findByUserIdAndFeedIdIn(userId, List.of(feedId)).stream()
                .anyMatch(b -> b.getFeedId().equals(feedId));

        boolean isReposted = feedRepostRepository.existsByUserIdAndSourceFeedId(userId, feedId);

        boolean isFollowing = !userId.equals(feed.getUserId()) &&
                followRepository.existsByFollowerIdAndFollowingId(userId, feed.getUserId());

        FeedMyStateResponse myState = FeedMyStateResponse.builder()
                .reaction(feedReaction != null ? feedReaction.getReactionType() : ReactionType.NONE)
                .isBookmarked(isBookmarked)
                .isReposted(isReposted)
                .isFollowing(isFollowing)
                .build();

        FeedDetailFeedResponse feedDetail = FeedDetailFeedResponse.builder()
                .feedId(feed.getId())
                .contentType(feed.getContentType())
                .isMine(isMine)
                .createdAt(feed.getCreatedAt())
                .user(userResponse)
                .content(contentResponse)
                .counts(countsResponse)
                .myState(myState)
                .build();

        List<FeedComment> topComments = feedCommentRepository.findByFeedIdAndParentCommentIdIsNullOrderByCreatedAtAsc(feedId);
        List<CommentItemResponse> comments = buildCommentResponses(topComments, userId);

        return FeedDetailResponse.builder()
                .feed(feedDetail)
                .comments(comments)
                .build();
    }

    private List<CommentItemResponse> buildCommentResponses(List<FeedComment> comments, Long currentUserId) {
        if (comments.isEmpty()) return List.of();

        List<Long> commentIds = comments.stream().map(FeedComment::getId).toList();
        Set<Long> authorIds = comments.stream().map(FeedComment::getUserId).collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findByIdIn(new ArrayList<>(authorIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, FeedCommentReaction> reactionMap = feedCommentReactionRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
                .collect(Collectors.toMap(FeedCommentReaction::getCommentId, r -> r));

        List<CommentItemResponse> result = new ArrayList<>();
        for (FeedComment c : comments) {
            List<FeedComment> replies = feedCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(c.getId());
            List<CommentItemResponse> replyResponses = buildCommentResponsesFlat(replies, currentUserId);

            User commentAuthor = userMap.get(c.getUserId());
            FeedCommentReaction reaction = reactionMap.get(c.getId());

            boolean isFollowing = !currentUserId.equals(c.getUserId()) &&
                    followRepository.existsByFollowerIdAndFollowingId(currentUserId, c.getUserId());

            result.add(CommentItemResponse.builder()
                    .commentId(c.getId())
                    .user(toFeedUserResponse(commentAuthor))
                    .mentionName(c.getMentionName())
                    .content(c.getContent())
                    .counts(CommentCountsResponse.builder()
                            .commentCount(c.getCommentCount())
                            .likeCount(c.getLikeCount())
                            .dislikeCount(c.getDislikeCount())
                            .quoteCount(c.getQuoteCount())
                            .build())
                    .myState(CommentMyStateResponse.builder()
                            .reaction(reaction != null ? reaction.getReactionType() : ReactionType.NONE)
                            .isBookmarked(false)
                            .isReposted(false)
                            .isFollowing(isFollowing)
                            .build())
                    .createdAt(c.getCreatedAt())
                    .commentReplies(replyResponses)
                    .build());
        }
        return result;
    }

    private List<CommentItemResponse> buildCommentResponsesFlat(List<FeedComment> comments, Long currentUserId) {
        if (comments.isEmpty()) return List.of();

        List<Long> commentIds = comments.stream().map(FeedComment::getId).toList();
        Set<Long> authorIds = comments.stream().map(FeedComment::getUserId).collect(Collectors.toSet());

        Map<Long, User> userMap = userRepository.findByIdIn(new ArrayList<>(authorIds)).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, FeedCommentReaction> reactionMap = feedCommentReactionRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
                .collect(Collectors.toMap(FeedCommentReaction::getCommentId, r -> r));

        List<CommentItemResponse> result = new ArrayList<>();
        for (FeedComment c : comments) {
            User commentAuthor = userMap.get(c.getUserId());
            FeedCommentReaction reaction = reactionMap.get(c.getId());

            boolean isFollowing = !currentUserId.equals(c.getUserId()) &&
                    followRepository.existsByFollowerIdAndFollowingId(currentUserId, c.getUserId());

            result.add(CommentItemResponse.builder()
                    .commentId(c.getId())
                    .user(toFeedUserResponse(commentAuthor))
                    .mentionName(c.getMentionName())
                    .content(c.getContent())
                    .counts(CommentCountsResponse.builder()
                            .commentCount(c.getCommentCount())
                            .likeCount(c.getLikeCount())
                            .dislikeCount(c.getDislikeCount())
                            .quoteCount(c.getQuoteCount())
                            .build())
                    .myState(CommentMyStateResponse.builder()
                            .reaction(reaction != null ? reaction.getReactionType() : ReactionType.NONE)
                            .isBookmarked(false)
                            .isReposted(false)
                            .isFollowing(isFollowing)
                            .build())
                    .createdAt(c.getCreatedAt())
                    .commentReplies(List.of())
                    .build());
        }
        return result;
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
        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));
        if (!feed.getUserId().equals(userId)) throw new ApiException(ErrorCode.FEED_DELETE_FORBIDDEN);

        List<FeedComment> comments = feedCommentRepository.findByFeedId(feedId);
        List<Long> commentIds = comments.stream().map(FeedComment::getId).toList();

        if (!commentIds.isEmpty()) {
            feedCommentReactionRepository.findByCommentIdIn(commentIds).forEach(feedCommentReactionRepository::delete);
        }

        feedCommentRepository.deleteAll(comments);
        feedReactionRepository.findByFeedId(feedId).forEach(feedReactionRepository::delete);
        feedBookmarkRepository.findByFeedId(feedId).forEach(feedBookmarkRepository::delete);
        feedRepostRepository.findBySourceFeedIdOrTargetFeedId(feedId).forEach(feedRepostRepository::delete);

        feedRepository.delete(feed);
    }

    @Transactional(readOnly = false)
    public CommentCreateResponse createComment(Long feedId, Long userId, CommentCreateRequest request) {
        feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));
        if (request.getContent() == null || request.getContent().isBlank()) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        FeedComment comment = FeedComment.builder()
                .feedId(feedId)
                .userId(userId)
                .parentCommentId(request.getParentCommentId())
                .content(request.getContent().trim())
                .mentionName(null)
                .build();

        comment = feedCommentRepository.save(comment);

        Feed feed = feedRepository.findById(feedId).orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));
        feed.setCommentCount((int) (feed.getCommentCount() + 1));
        feedRepository.save(feed);

        if (request.getParentCommentId() != null) {
            feedCommentRepository.findById(request.getParentCommentId()).ifPresent(parent -> {
                parent.setCommentCount(parent.getCommentCount() + 1);
                feedCommentRepository.save(parent);
            });
        }

        return CommentCreateResponse.builder()
                .commentId(comment.getId())
                .feedId(feedId)
                .parentCommentId(request.getParentCommentId())
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
    public Long createPostFeed(Long userId, String content, List<String> imageUrls, String hashtags) {
        if (!StringUtils.hasText(content)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        Feed feed = Feed.builder()
                .userId(userId)
                .kind(FeedKind.ORIGINAL)
                .contentType(FeedContentType.POST)
                .content(content.trim())
                .hashtags((hashtags == null) ? "[]" : hashtags)
                .build();

        Feed saved = feedRepository.save(feed);

        return saved.getId();
    }

    @Transactional
    public Long createReviewFeed(Long userId, String content, String vendor, Float rating, String productUrl,
                                 List<String> imageUrls, String hashtags) {
        if (!StringUtils.hasText(content)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        if (!StringUtils.hasText(vendor)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        Feed feed = Feed.builder()
                .userId(userId)
                .kind(FeedKind.ORIGINAL)
                .contentType(FeedContentType.REVIEW)
                .content(content.trim())
                .vendor(vendor.trim())
                .productUrl(productUrl)
                .rating(rating == null ? null : new java.math.BigDecimal(String.valueOf(rating)))
                .hashtags((hashtags == null) ? "[]" : hashtags)
                .build();

        Feed saved = feedRepository.save(feed);

        return saved.getId();
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
                .contentType(source.getContentType())
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

        return saved.getId();
    }

    @Transactional
    public Long createQuote(Long userId, Long sourceFeedId, String quoteText) {
        feedRepository.findById(sourceFeedId)
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

        return feedRepository.save(quoteFeed).getId();
    }

    @Transactional
    public void updatePostFeed(Long feedId, Long userId, String content, String hashtags, List<String> imageUrls) {
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

        if (hashtags != null) feed.setHashtags(hashtags);

        feedRepository.save(feed);
    }

    @Transactional
    public void updateReviewFeed(Long feedId, Long userId, String content, String vendor, Float rating, String productUrl,
                                 String hashtags, List<String> imageUrls) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new ApiException(ErrorCode.FEED_NOT_FOUND));

        if (!feed.getUserId().equals(userId)) throw new ApiException(ErrorCode.FEED_UPDATE_FORBIDDEN);
        if (feed.getKind() != FeedKind.ORIGINAL) throw new ApiException(ErrorCode.FEED_UPDATE_FORBIDDEN);

        if (feed.getContentType() != FeedContentType.REVIEW) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        if (!StringUtils.hasText(content) || !StringUtils.hasText(vendor)) throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);

        feed.setContent(content.trim());
        feed.setVendor(vendor.trim());
        feed.setProductUrl(productUrl);
        feed.setRating(rating == null ? null : new java.math.BigDecimal(String.valueOf(rating)));
        if (hashtags != null) feed.setHashtags(hashtags);

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
    }
}

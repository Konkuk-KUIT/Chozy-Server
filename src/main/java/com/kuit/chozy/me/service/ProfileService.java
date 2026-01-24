package com.kuit.chozy.me.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.bookmark.domain.Bookmark;
import com.kuit.chozy.bookmark.repository.BookmarkRepository;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.dto.response.BookmarkAuthorResponse;
import com.kuit.chozy.me.dto.response.BookmarkItemResponse;
import com.kuit.chozy.me.dto.response.BookmarkListResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.me.dto.response.ReviewItemResponse;
import com.kuit.chozy.me.dto.response.ReviewListResponse;
import com.kuit.chozy.post.domain.Post;
import com.kuit.chozy.post.repository.PostRepository;
import com.kuit.chozy.user.domain.User;

import com.kuit.chozy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    @Transactional(readOnly = true)
    public ProfileResponseDto getMyProfile(String loginId) {
        User user = getActiveUser(loginId);
        return ProfileResponseDto.from(user);
    }

    @Transactional
    public ProfileResponseDto updateMyProfile(
            String loginId,
            ProfileUpdateDto request
    ) {
        User user = getActiveUser(loginId);

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
            user.setWeightPublic(request.getIsWeightPublic());

        return ProfileResponseDto.from(user);
    }

    @Transactional(readOnly = true)
    public ReviewListResponse getMyReviews(String loginId, int page, int size) {
        User user = getActiveUser(loginId);
        
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));
        
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Post> postPage = postRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        
        return buildReviewListResponse(postPage);
    }

    @Transactional(readOnly = true)
    public ReviewListResponse searchMyReviews(String loginId, String keyword, int page, int size) {
        User user = getActiveUser(loginId);
        
        if (keyword == null || keyword.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }
        
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));
        
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Post> postPage = postRepository.findByUserIdAndContentContainingOrderByCreatedAtDesc(
                user.getId(), keyword.trim(), pageable
        );
        
        return buildReviewListResponse(postPage);
    }

    @Transactional(readOnly = true)
    public BookmarkListResponse getMyBookmarks(String loginId, int page, int size) {
        User user = getActiveUser(loginId);

        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        if (bookmarkPage.isEmpty()) {
            return new BookmarkListResponse(
                    List.of(),
                    bookmarkPage.getNumber(),
                    bookmarkPage.getSize(),
                    bookmarkPage.getTotalElements(),
                    bookmarkPage.getTotalPages(),
                    bookmarkPage.hasNext()
            );
        }

        return buildBookmarkListResponse(bookmarkPage);
    }

    private ReviewListResponse buildReviewListResponse(Page<Post> postPage) {
        List<ReviewItemResponse> items = postPage.getContent().stream()
                .map(post -> new ReviewItemResponse(
                        post.getId(),
                        post.getContent(),
                        post.getImageUrls(),
                        post.getLikeCount(),
                        post.getDislikeCount(),
                        post.getCommentCount(),
                        post.getQuoteCount(),
                        post.getCreatedAt()
                ))
                .toList();
        
        return new ReviewListResponse(
                items,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages(),
                postPage.hasNext()
        );
    }

    private BookmarkListResponse buildBookmarkListResponse(Page<Bookmark> bookmarkPage) {
        List<Long> postIds = bookmarkPage.getContent().stream()
                .map(Bookmark::getPostId)
                .distinct()
                .toList();

        Map<Long, Post> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));

        List<Long> authorIds = postMap.values().stream()
                .map(Post::getUserId)
                .distinct()
                .toList();

        Map<Long, User> authorMap = userRepository.findByIdIn(authorIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<BookmarkItemResponse> items = bookmarkPage.getContent().stream()
                .map(bookmark -> toBookmarkItem(bookmark, postMap, authorMap))
                .filter(Objects::nonNull)
                .toList();

        return new BookmarkListResponse(
                items,
                bookmarkPage.getNumber(),
                bookmarkPage.getSize(),
                bookmarkPage.getTotalElements(),
                bookmarkPage.getTotalPages(),
                bookmarkPage.hasNext()
        );
    }

    private BookmarkItemResponse toBookmarkItem(
            Bookmark bookmark,
            Map<Long, Post> postMap,
            Map<Long, User> authorMap
    ) {
        Post post = postMap.get(bookmark.getPostId());
        if (post == null) {
            return null;
        }

        User author = authorMap.get(post.getUserId());
        BookmarkAuthorResponse authorResponse = null;
        if (author != null) {
            authorResponse = new BookmarkAuthorResponse(
                    author.getId(),
                    author.getLoginId(),
                    author.getNickname(),
                    author.getProfileImageUrl()
            );
        }

        return new BookmarkItemResponse(
                post.getId(),
                post.getContent(),
                post.getImageUrls(),
                authorResponse,
                post.getLikeCount(),
                post.getDislikeCount(),
                post.getCommentCount(),
                post.getQuoteCount(),
                true,
                bookmark.getCreatedAt(),
                post.getCreatedAt()
        );
    }

    private User getActiveUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        return user;
    }
}
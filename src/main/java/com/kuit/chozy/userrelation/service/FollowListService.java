package com.kuit.chozy.userrelation.service;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.domain.Block;
import com.kuit.chozy.userrelation.domain.Follow;
import com.kuit.chozy.userrelation.domain.FollowRequest;
import com.kuit.chozy.userrelation.domain.FollowRequestStatus;
import com.kuit.chozy.userrelation.dto.FollowStatus;
import com.kuit.chozy.userrelation.dto.response.FollowerItemResponse;
import com.kuit.chozy.userrelation.dto.response.FollowerListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowingItemResponse;
import com.kuit.chozy.userrelation.dto.response.FollowingListResponse;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FollowListService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final BlockRepository blockRepository;

    public FollowListService(
            UserRepository userRepository,
            FollowRepository followRepository,
            FollowRequestRepository followRequestRepository,
            BlockRepository blockRepository
    ) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.followRequestRepository = followRequestRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional(readOnly = true)
    public FollowerListResponse getFollowers(Long userId, int page, int size) {

        validatePage(page, size);

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        if (!me.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Follow> followerPage =
                followRepository.findByFollowingId(userId, pageable);

        if (followerPage.isEmpty()) {
            return new FollowerListResponse(
                    List.of(),
                    followerPage.getNumber(),
                    followerPage.getSize(),
                    followerPage.getTotalElements(),
                    followerPage.getTotalPages(),
                    followerPage.hasNext()
            );
        }

        List<Long> followerIds = followerPage.getContent().stream()
                .map(Follow::getFollowerId)
                .toList();

        Map<Long, User> userMap = userRepository.findByIdIn(followerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Set<Long> myFollowingSet = followRepository
                .findByFollowerIdAndFollowingIdIn(userId, followerIds)
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(userId, followerIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> blockedSet = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(userId, followerIds)
                .stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toSet());

        // close-friends 테이블이 아직 없다고 가정 → false 고정
        Set<Long> closeFriendSet = Set.of();

        List<FollowerItemResponse> items = followerPage.getContent().stream()
                .map(follow -> {
                    Long followerId = follow.getFollowerId();
                    User u = userMap.get(followerId);

                    if (u == null) {
                        return null;
                    }

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            myFollowingSet.contains(followerId),
                            pendingRequestSet.contains(followerId)
                    );

                    boolean isFollowing = myFollowingSet.contains(followerId);

                    return new FollowerItemResponse(
                            followerId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            u.isAccountPublic(),
                            isFollowing,
                            myFollowStatus,
                            blockedSet.contains(followerId),
                            closeFriendSet.contains(followerId),
                            follow.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new FollowerListResponse(
                items,
                followerPage.getNumber(),
                followerPage.getSize(),
                followerPage.getTotalElements(),
                followerPage.getTotalPages(),
                followerPage.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public FollowingListResponse getFollowings(Long userId, int page, int size) {

        validatePage(page, size);

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        if (!me.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Follow> followingPage =
                followRepository.findByFollowerId(userId, pageable);

        if (followingPage.isEmpty()) {
            return new FollowingListResponse(
                    List.of(),
                    followingPage.getNumber(),
                    followingPage.getSize(),
                    followingPage.getTotalElements(),
                    followingPage.getTotalPages(),
                    followingPage.hasNext()
            );
        }

        List<Long> followingIds = followingPage.getContent().stream()
                .map(Follow::getFollowingId)
                .toList();

        Map<Long, User> userMap = userRepository.findByIdIn(followingIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Set<Long> myFollowingSet = new HashSet<>(followingIds);

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(userId, followingIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> followingMeSet = followRepository
                .findByFollowerIdInAndFollowingId(followingIds, userId)
                .stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toSet());

        Set<Long> blockedSet = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(userId, followingIds)
                .stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toSet());

        Set<Long> closeFriendSet = Set.of();

        List<FollowingItemResponse> items = followingPage.getContent().stream()
                .map(follow -> {
                    Long followingId = follow.getFollowingId();
                    User u = userMap.get(followingId);

                    if (u == null) {
                        return null;
                    }

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            myFollowingSet.contains(followingId),
                            pendingRequestSet.contains(followingId)
                    );

                    boolean isFollowedByMe = myFollowStatus == FollowStatus.FOLLOWING;

                    return new FollowingItemResponse(
                            followingId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            u.isAccountPublic(),
                            myFollowStatus,
                            isFollowedByMe,
                            followingMeSet.contains(followingId),
                            blockedSet.contains(followingId),
                            closeFriendSet.contains(followingId),
                            follow.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new FollowingListResponse(
                items,
                followingPage.getNumber(),
                followingPage.getSize(),
                followingPage.getTotalElements(),
                followingPage.getTotalPages(),
                followingPage.hasNext()
        );
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        if (size <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        if (size > 50) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
    }

    private FollowStatus computeMyFollowStatus(boolean isFollowing, boolean isRequested) {
        if (isFollowing) {
            return FollowStatus.FOLLOWING;
        }
        if (isRequested) {
            return FollowStatus.REQUESTED;
        }
        return FollowStatus.NONE;
    }
}
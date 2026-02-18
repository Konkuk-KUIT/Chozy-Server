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
    public FollowerListResponse getFollowers(Long viewerId, Long targetUserId, int page, int size) {
        validatePage(page, size);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        if (!target.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        validateViewPermission(viewerId, target);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Follow> followerPage =
                followRepository.findByFollowingId(targetUserId, pageable);

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
                .findByFollowerIdAndFollowingIdIn(viewerId, followerIds)
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(viewerId, followerIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> blockedSet = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(viewerId, followerIds)
                .stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toSet());

        Set<Long> closeFriendSet = Set.of();

        List<FollowerItemResponse> items = followerPage.getContent().stream()
                .map(follow -> {
                    Long followerId = follow.getFollowerId();
                    User u = userMap.get(followerId);

                    if (u == null) {
                        return null;
                    }

                    boolean isFollowingByMe = myFollowingSet.contains(followerId);
                    boolean isFollowingMe = Objects.equals(viewerId, targetUserId);

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            isFollowingByMe,
                            pendingRequestSet.contains(followerId)
                    );

                    return new FollowerItemResponse(
                            followerId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            Boolean.TRUE.equals(u.getIsAccountPublic()),
                            myFollowStatus,
                            isFollowingByMe,
                            isFollowingMe,
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
    public FollowingListResponse getFollowings(Long viewerId, Long targetUserId, int page, int size) {
        validatePage(page, size);

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        if (!target.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        validateViewPermission(viewerId, target);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );

        Page<Follow> followingPage =
                followRepository.findByFollowerId(targetUserId, pageable);

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

        Set<Long> myFollowingSet = followRepository
                .findByFollowerIdAndFollowingIdIn(viewerId, followingIds)
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(viewerId, followingIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> followingMeSet = followRepository
                .findByFollowerIdInAndFollowingId(followingIds, viewerId)
                .stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toSet());

        Set<Long> blockedSet = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(viewerId, followingIds)
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

                    boolean isFollowingByMe = myFollowingSet.contains(followingId);

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            isFollowingByMe,
                            pendingRequestSet.contains(followingId)
                    );

                    return new FollowingItemResponse(
                            followingId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            Boolean.TRUE.equals(u.getIsAccountPublic()),
                            myFollowStatus,
                            isFollowingByMe,
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

    private void validateViewPermission(Long viewerId, User target) {
        if (Boolean.TRUE.equals(target.getIsAccountPublic())) {
            return;
        }

        if (viewerId.equals(target.getId())) {
            return;
        }

        boolean isFollower = followRepository.existsByFollowerIdAndFollowingIdAndStatus(
                viewerId,
                target.getId(),
                FollowStatus.FOLLOWING
        );

        if (isFollower) {
            return;
        }

        throw new ApiException(ErrorCode.PRIVATE_ACCOUNT_FORBIDDEN);
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0 || size > 50) {
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
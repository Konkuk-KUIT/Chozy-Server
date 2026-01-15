package com.kuit.chozy.userrelation.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.dto.*;
import com.kuit.chozy.userrelation.domain.Block;
import com.kuit.chozy.userrelation.domain.Follow;
import com.kuit.chozy.userrelation.domain.FollowRequest;
import com.kuit.chozy.userrelation.domain.FollowRequestStatus;
import com.kuit.chozy.userrelation.dto.response.FollowerItemResponse;
import com.kuit.chozy.userrelation.dto.response.FollowerListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowingItemResponse;
import com.kuit.chozy.userrelation.dto.response.FollowingListResponse;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public FollowerListResponse getFollowers(Long meId, int page, int size) {

        validatePage(page, size);

        User me = userRepository.findById(meId)
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
                followRepository.findByFollowingId(meId, pageable);

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

        Set<Long> myFollowingSet = followRepository.findByFollowerIdAndFollowingIdIn(meId, followerIds).stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(meId, followerIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> blockedSet = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(meId, followerIds)
                .stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toSet());

        // close-friends 테이블이 아직 없다고 가정 → false 고정
        Set<Long> closeFriendSet = Set.of();

        List<FollowerItemResponse> items = followerPage.getContent().stream()
                .map(follow -> {
                    Long userId = follow.getFollowerId();
                    User u = userMap.get(userId);

                    // user row가 없으면 방어적으로 스킵 대신 최소값 처리(원하면 예외로 바꿔도 됨)
                    if (u == null) {
                        return null;
                    }

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            myFollowingSet.contains(userId),
                            pendingRequestSet.contains(userId)
                    );

                    boolean isFollowing = myFollowingSet.contains(userId);

                    return new FollowerItemResponse(
                            userId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            u.isAccountPublic(),
                            isFollowing,
                            myFollowStatus,
                            blockedSet.contains(userId),
                            closeFriendSet.contains(userId),
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
    public FollowingListResponse getFollowings(Long meId, int page, int size) {

        validatePage(page, size);

        User me = userRepository.findById(meId)
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
                followRepository.findByFollowerId(meId, pageable);

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

        // 내가 팔로우 중인 리스트는 기본적으로 전부 FOLLOWING이긴 하지만,
        // 명세에 myFollowStatus가 들어가서 "FOLLOWING"으로 넣는다.
        Set<Long> myFollowingSet = new HashSet<>(followingIds);

        // 혹시 follow 테이블 없고 요청 상태만 존재하는 케이스가 있으면 REQUESTED가 필요하지만,
        // followings 목록은 "실제 follow" 기준이므로 보통 REQUESTED는 여기서 안 뜸.
        // 그래도 명세 필드 유지 목적이라 pendingSet을 넣어 계산해둠.
        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(meId, followingIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        // 상대가 나를 팔로우하는지(맞팔)
        Set<Long> followingMeSet = followRepository.findByFollowerIdInAndFollowingId(followingIds, meId).stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toSet());

        Set<Long> blockedSet = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(meId, followingIds)
                .stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toSet());

        Set<Long> closeFriendSet = Set.of();

        List<FollowingItemResponse> items = followingPage.getContent().stream()
                .map(follow -> {
                    Long userId = follow.getFollowingId();
                    User u = userMap.get(userId);

                    if (u == null) {
                        return null;
                    }

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            myFollowingSet.contains(userId),
                            pendingRequestSet.contains(userId)
                    );

                    boolean isFollowedByMe = myFollowStatus == FollowStatus.FOLLOWING;

                    return new FollowingItemResponse(
                            userId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            u.isAccountPublic(),
                            myFollowStatus,
                            isFollowedByMe,
                            followingMeSet.contains(userId),
                            blockedSet.contains(userId),
                            closeFriendSet.contains(userId),
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

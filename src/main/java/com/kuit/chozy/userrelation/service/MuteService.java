package com.kuit.chozy.userrelation.service;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.domain.Follow;
import com.kuit.chozy.userrelation.domain.FollowRequest;
import com.kuit.chozy.userrelation.domain.FollowRequestStatus;
import com.kuit.chozy.userrelation.domain.Block;
import com.kuit.chozy.userrelation.domain.Mute;
import com.kuit.chozy.userrelation.dto.FollowStatus;
import com.kuit.chozy.userrelation.dto.response.FollowingItemResponse;
import com.kuit.chozy.userrelation.dto.response.MuteResponse;
import com.kuit.chozy.userrelation.dto.response.MutedUserListResponse;
import com.kuit.chozy.userrelation.dto.response.UnmuteResponse;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import com.kuit.chozy.userrelation.repository.MuteRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class MuteService {

    private final MuteRepository muteRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final BlockRepository blockRepository;

    public MuteService(
            MuteRepository muteRepository,
            UserRepository userRepository,
            FollowRepository followRepository,
            FollowRequestRepository followRequestRepository,
            BlockRepository blockRepository
    ) {
        this.muteRepository = muteRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.followRequestRepository = followRequestRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public MuteResponse mute(Long userId, Long targetUserId) {
        validateMuteTarget(userId, targetUserId);

        Mute mute = muteRepository
                .findByMuterIdAndMutedId(userId, targetUserId)
                .orElseGet(() -> new Mute(userId, targetUserId));

        mute.activate();
        Mute saved = muteRepository.save(mute);

        return new MuteResponse(targetUserId, true, saved.getMutedAt());
    }

    @Transactional
    public UnmuteResponse unmute(Long userId, Long targetUserId) {
        validateUnmuteTarget(userId, targetUserId);

        Mute mute = muteRepository
                .findByMuterIdAndMutedIdAndActiveTrue(userId, targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_MUTED));

        mute.deactivate();
        muteRepository.save(mute);

        return new UnmuteResponse(targetUserId, false, LocalDateTime.now());
    }

    @Transactional
    public MutedUserListResponse getMutedUsers(Long userId, int page, int size) {
        validatePage(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("mutedAt").descending()
        );

        Page<Mute> mutePage =
                muteRepository.findByMuterIdAndActiveTrueOrderByMutedAtDesc(userId, pageable);

        if (mutePage.isEmpty()) {
            return new MutedUserListResponse(
                    List.of(),
                    mutePage.getNumber(),
                    mutePage.getSize(),
                    mutePage.getTotalElements(),
                    mutePage.getTotalPages(),
                    mutePage.hasNext()
            );
        }

        List<Long> mutedIds = mutePage.getContent().stream()
                .map(Mute::getMutedId)
                .toList();

        Map<Long, User> userMap = userRepository.findByIdIn(mutedIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Set<Long> myFollowingSet = followRepository
                .findByFollowerIdAndFollowingIdInAndStatus(userId, mutedIds, FollowStatus.FOLLOWING)
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(userId, mutedIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> followingMeSet = followRepository
                .findByFollowerIdInAndFollowingIdAndStatus(mutedIds, userId, FollowStatus.FOLLOWING)
                .stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toSet());

        Set<Long> blockedByMe = blockRepository
                .findByBlockerIdAndBlockedIdInAndActiveTrue(userId, mutedIds)
                .stream()
                .map(Block::getBlockedId)
                .collect(Collectors.toSet());

        Set<Long> closeFriendSet = Set.of();

        List<FollowingItemResponse> items = mutePage.getContent().stream()
                .map(m -> {
                    Long mutedUserId = m.getMutedId();
                    User u = userMap.get(mutedUserId);
                    if (u == null) {
                        return null;
                    }

                    FollowStatus myFollowStatus = computeMyFollowStatus(
                            myFollowingSet.contains(mutedUserId),
                            pendingRequestSet.contains(mutedUserId)
                    );

                    boolean isFollowedByMe = myFollowStatus == FollowStatus.FOLLOWING;

                    return new FollowingItemResponse(
                            mutedUserId,
                            u.getLoginId(),
                            u.getNickname(),
                            u.getProfileImageUrl(),
                            Boolean.TRUE.equals(u.getIsAccountPublic()),
                            myFollowStatus,
                            isFollowedByMe,
                            followingMeSet.contains(mutedUserId),
                            blockedByMe.contains(mutedUserId),
                            closeFriendSet.contains(mutedUserId),
                            null
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new MutedUserListResponse(
                items,
                mutePage.getNumber(),
                mutePage.getSize(),
                mutePage.getTotalElements(),
                mutePage.getTotalPages(),
                mutePage.hasNext()
        );
    }

    private void validateMuteTarget(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_MUTE_NOT_ALLOWED);
        }
        if (!userRepository.existsById(targetUserId)) {
            throw new ApiException(ErrorCode.TARGET_USER_NOT_FOUND);
        }
    }

    private void validateUnmuteTarget(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_UNMUTE_NOT_ALLOWED);
        }
        if (!userRepository.existsById(targetUserId)) {
            throw new ApiException(ErrorCode.TARGET_USER_NOT_FOUND);
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        if (size <= 0 || size > 50) {
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

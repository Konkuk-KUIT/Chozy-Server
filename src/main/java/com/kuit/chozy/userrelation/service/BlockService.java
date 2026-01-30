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
import com.kuit.chozy.userrelation.dto.response.BlockResponse;
import com.kuit.chozy.userrelation.dto.response.BlockedUserListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowingItemResponse;
import com.kuit.chozy.userrelation.dto.response.UnblockResponse;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;

    public BlockService(
            BlockRepository blockRepository,
            UserRepository userRepository,
            FollowRepository followRepository,
            FollowRequestRepository followRequestRepository
    ) {
        this.blockRepository = blockRepository;
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.followRequestRepository = followRequestRepository;
    }

    @Transactional
    public BlockResponse block(Long meId, Long targetUserId) {
        validateBlockTarget(meId, targetUserId);

        // 재차단(soft delete) 대비: active 조건 없이 조회해야 유니크 충돌 방지
        Block block = blockRepository
                .findByBlockerIdAndBlockedId(meId, targetUserId)
                .orElseGet(() -> new Block(meId, targetUserId));

        // 멱등 처리 + blockedAt 갱신
        block.activate();

        Block saved = blockRepository.save(block);

        cleanupFollowRelations(meId, targetUserId);

        return new BlockResponse(targetUserId, true, saved.getBlockedAt());
    }

    @Transactional
    public UnblockResponse unblock(Long meId, Long targetUserId) {
        validateUnblockTarget(meId, targetUserId);

        Block block = blockRepository.findByBlockerIdAndBlockedIdAndActiveTrue(meId, targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_BLOCKED));

        block.deactivate();
        blockRepository.save(block);

        return new UnblockResponse(targetUserId, false, LocalDateTime.now());
    }

    @Transactional
    public BlockedUserListResponse getBlockedUsers(Long meId, int page, int size) {

        validatePage(page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("blockedAt").descending()
        );

        Page<Block> blockPage = blockRepository.findByBlockerIdAndActiveTrueOrderByBlockedAtDesc(meId, pageable);

        if (blockPage.isEmpty()) {
            return new BlockedUserListResponse(
                    List.of(),
                    blockPage.getNumber(),
                    blockPage.getSize(),
                    blockPage.getTotalElements(),
                    blockPage.getTotalPages(),
                    blockPage.hasNext()
            );
        }

        List<Long> blockedIds = blockPage.getContent().stream()
                .map(Block::getBlockedId)
                .toList();

        Map<Long, User> userMap = userRepository.findByIdIn(blockedIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Set<Long> myFollowingSet = followRepository.findByFollowerIdAndFollowingIdIn(meId, blockedIds).stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toSet());

        Set<Long> pendingRequestSet = followRequestRepository
                .findByRequesterIdAndTargetIdInAndStatus(meId, blockedIds, FollowRequestStatus.PENDING)
                .stream()
                .map(FollowRequest::getTargetId)
                .collect(Collectors.toSet());

        Set<Long> followingMeSet = followRepository.findByFollowerIdInAndFollowingId(blockedIds, meId).stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toSet());

        Set<Long> closeFriendSet = Set.of();

        List<FollowingItemResponse> items = blockPage.getContent().stream()
                .map(b -> {
                    Long userId = b.getBlockedId();
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
                            true,
                            closeFriendSet.contains(userId),
                            null
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new BlockedUserListResponse(
                items,
                blockPage.getNumber(),
                blockPage.getSize(),
                blockPage.getTotalElements(),
                blockPage.getTotalPages(),
                blockPage.hasNext()
        );
    }

    private void validateBlockTarget(Long meId, Long targetUserId) {
        if (meId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_BLOCK_NOT_ALLOWED);
        }
        if (!userRepository.existsById(targetUserId)) {
            throw new ApiException(ErrorCode.TARGET_USER_NOT_FOUND);
        }
    }

    private void validateUnblockTarget(Long meId, Long targetUserId) {
        if (meId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_UNBLOCK_NOT_ALLOWED);
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

    private void cleanupFollowRelations(Long meId, Long targetUserId) {
        // (a) 내가 상대를 팔로우 중이면 -> INACTIVE
        followRepository.updateStatus(meId, targetUserId, FollowStatus.FOLLOWING, FollowStatus.INACTIVE);

        // (b) 상대가 나를 팔로우 중이면 -> INACTIVE
        followRepository.updateStatus(targetUserId, meId, FollowStatus.FOLLOWING, FollowStatus.INACTIVE);

        // FollowRequest가 따로 존재하므로 "요청 정리"는 FollowRequestRepository에서 처리하는 게 정합성 맞음
        followRequestRepository.deletePending(meId, targetUserId);
        followRequestRepository.deletePending(targetUserId, meId);
    }
}

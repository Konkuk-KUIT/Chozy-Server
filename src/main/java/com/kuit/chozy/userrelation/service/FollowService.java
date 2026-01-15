package com.kuit.chozy.userrelation.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.dto.response.FollowActionResponse;
import com.kuit.chozy.userrelation.dto.FollowStatus;
import com.kuit.chozy.userrelation.domain.Follow;
import com.kuit.chozy.userrelation.domain.FollowRequest;
import com.kuit.chozy.userrelation.domain.FollowRequestStatus;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final FollowRequestRepository followRequestRepository;
    private final BlockRepository blockRepository;

    public FollowService(
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

    @Transactional
    public FollowActionResponse follow(Long meId, Long targetUserId) {

        if (meId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (meId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        User me = userRepository.findById(meId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        if (!me.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.TARGET_USER_NOT_FOUND));

        // 내가 상대를 차단한 경우
        if (blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(meId, targetUserId)) {
            throw new ApiException(ErrorCode.CANNOT_FOLLOW_BLOCKED_USER);
        }

        // 상대가 나를 차단한 경우
        if (blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(targetUserId, meId)) {
            throw new ApiException(ErrorCode.CANNOT_FOLLOW_BLOCKED_USER);
        }

        if (followRepository.existsByFollowerIdAndFollowingId(meId, targetUserId)) {
            throw new ApiException(ErrorCode.ALREADY_FOLLOWING);
        }

        // 공개 계정이면 즉시 팔로우
        if (target.isAccountPublic()) {
            Follow follow = new Follow(meId, targetUserId, LocalDateTime.now());
            followRepository.save(follow);

            return new FollowActionResponse(
                    targetUserId,
                    FollowStatus.FOLLOWING,
                    null,
                    null
            );
        }

        // 비공개 계정이면 PENDING만 중복 금지 (REJECTED/CANCELED는 재요청 허용)
        boolean pendingExists = followRequestRepository.existsByRequesterIdAndTargetIdAndStatus(
                meId,
                targetUserId,
                FollowRequestStatus.PENDING
        );

        if (pendingExists) {
            throw new ApiException(ErrorCode.ALREADY_REQUESTED);
        }

        FollowRequest request = new FollowRequest(
                meId,
                targetUserId,
                FollowRequestStatus.PENDING,
                LocalDateTime.now()
        );

        FollowRequest saved = followRequestRepository.save(request);

        return new FollowActionResponse(
                targetUserId,
                FollowStatus.REQUESTED,
                saved.getId(),
                saved.getRequestedAt()
        );
    }

    @Transactional
    public FollowActionResponse unfollowOrCancel(Long meId, Long targetUserId) {

        if (meId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (meId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }

        User me = userRepository.findById(meId)
                .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED));

        if (!me.isActive()) {
            throw new ApiException(ErrorCode.DEACTIVATED_ACCOUNT);
        }

        userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.TARGET_USER_NOT_FOUND));

        // 1) 이미 팔로우 중이면 언팔로우
        return followRepository.findByFollowerIdAndFollowingId(meId, targetUserId)
                .map(follow -> {
                    followRepository.delete(follow);
                    return new FollowActionResponse(targetUserId, FollowStatus.NONE, null, null);
                })
                .orElseGet(() -> {
                    // 2) PENDING 요청이 있으면 취소(CANCELED로 변경)
                    return followRequestRepository.findByRequesterIdAndTargetIdAndStatus(
                                    meId, targetUserId, FollowRequestStatus.PENDING
                            )
                            .map(req -> {
                                req.changeStatus(FollowRequestStatus.CANCELED);
                                return new FollowActionResponse(targetUserId, FollowStatus.NONE, null, null);
                            })
                            // 3) 둘 다 없으면 그냥 성공 처리
                            .orElse(new FollowActionResponse(targetUserId, FollowStatus.NONE, null, null));
                });
    }
}

package com.kuit.chozy.userrelation.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.repository.UserRepository;
import com.kuit.chozy.userrelation.domain.CloseFriend;
import com.kuit.chozy.userrelation.dto.response.CloseFriendItemResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendListResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendSetResponse;
import com.kuit.chozy.userrelation.dto.response.CloseFriendUnsetResponse;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.CloseFriendRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CloseFriendService {

    private final CloseFriendRepository closeFriendRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;

    public CloseFriendService(
            CloseFriendRepository closeFriendRepository,
            UserRepository userRepository,
            BlockRepository blockRepository
    ) {
        this.closeFriendRepository = closeFriendRepository;
        this.userRepository = userRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public CloseFriendUnsetResponse unsetCloseFriend(Long meUserId, Long targetUserId) {

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.TARGET_USER_NOT_FOUND));

        // 비활성화 계정 체크가 필요하면 추가
        // if (targetUser.getStatus() == UserStatus.INACTIVE) throw new ApiException(ErrorCode.USER_INACTIVE);

        // 차단 관계 존재 여부 (양방향, active=true)
        boolean blocked =
                blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(meUserId, targetUserId)
                        || blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(targetUserId, meUserId);

        if (blocked) {
            throw new ApiException(ErrorCode.BLOCK_RELATION_EXISTS);
        }

        // 멱등 처리: 있으면 삭제, 없으면 그냥 성공
        closeFriendRepository.findByUserIdAndTargetUserId(meUserId, targetUserId)
                .ifPresent(existing -> closeFriendRepository.delete(existing));
        // 또는:
        // closeFriendRepository.deleteByUserIdAndTargetUserId(meUserId, targetUserId);

        return new CloseFriendUnsetResponse(
                targetUserId,
                false,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public CloseFriendListResponse getCloseFriends(Long meUserId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));

        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<CloseFriendItemResponse> result = closeFriendRepository.findCloseFriendItems(meUserId, pageable);

        return new CloseFriendListResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }


    @Transactional
    public CloseFriendSetResponse setCloseFriend(Long meUserId, Long targetUserId) {

        // 자기 자신 설정 불가
        if (meUserId.equals(targetUserId)) {
            throw new ApiException(ErrorCode.SELF_CLOSE_FRIEND_NOT_ALLOWED);
        }

        // 대상 사용자 존재 여부
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.TARGET_USER_NOT_FOUND));

        // 비활성화 계정 체크 (UserStatus 사용 중이면 여기서)
        // if (targetUser.getStatus() == UserStatus.INACTIVE) {
        //     throw new ApiException(ErrorCode.USER_INACTIVE);
        // }

        // 차단 관계 존재 여부 (양방향, active = true)
        boolean blocked =
                blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(meUserId, targetUserId)
                        || blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(targetUserId, meUserId);

        if (blocked) {
            throw new ApiException(ErrorCode.BLOCK_RELATION_EXISTS);
        }

        // 이미 친한 친구면 멱등 성공 처리
        return closeFriendRepository.findByUserIdAndTargetUserId(meUserId, targetUserId)
                .map(existing -> new CloseFriendSetResponse(
                        existing.getTargetUserId(),
                        true,
                        existing.getSetAt()
                ))
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    CloseFriend saved =
                            closeFriendRepository.save(new CloseFriend(meUserId, targetUserId, now));

                    return new CloseFriendSetResponse(
                            saved.getTargetUserId(),
                            true,
                            saved.getSetAt()
                    );
                });
    }
}

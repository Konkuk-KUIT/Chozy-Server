package com.kuit.chozy.userrelation.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.userrelation.domain.Follow;
import com.kuit.chozy.userrelation.domain.FollowRequest;
import com.kuit.chozy.userrelation.dto.request.FollowRequestProcessRequest;
import com.kuit.chozy.userrelation.dto.response.FollowRequestListItemResponse;
import com.kuit.chozy.userrelation.dto.response.FollowRequestListResponse;
import com.kuit.chozy.userrelation.dto.response.FollowRequestProcessResponse;
import com.kuit.chozy.userrelation.repository.BlockRepository;
import com.kuit.chozy.userrelation.repository.FollowRepository;
import com.kuit.chozy.userrelation.repository.FollowRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FollowRequestService {

    private final FollowRequestRepository followRequestRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    public FollowRequestService(
            FollowRequestRepository followRequestRepository,
            FollowRepository followRepository,
            BlockRepository blockRepository
    ) {
        this.followRequestRepository = followRequestRepository;
        this.followRepository = followRepository;
        this.blockRepository = blockRepository;
    }

    @Transactional
    public FollowRequestProcessResponse process(
            Long meUserId,
            Long requestId,
            FollowRequestProcessRequest.ProcessStatus action
    ) {

        FollowRequest request = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new ApiException(ErrorCode.FOLLOW_REQUEST_NOT_FOUND));

        // 내가 받은 요청인지 검증 (targetId = me)
        if (!request.getTargetId().equals(meUserId)) {
            throw new ApiException(ErrorCode.FOLLOW_REQUEST_FORBIDDEN);
        }

        // 이미 처리된 요청인지 검증
        if (!request.isPending()) {
            throw new ApiException(ErrorCode.FOLLOW_REQUEST_ALREADY_PROCESSED);
        }

        // 차단 관계 존재 여부 (양방향, active=true)
        boolean blocked =
                blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(
                        meUserId, request.getRequesterId())
                        || blockRepository.existsByBlockerIdAndBlockedIdAndActiveTrue(
                        request.getRequesterId(), meUserId);

        if (blocked) {
            throw new ApiException(ErrorCode.BLOCK_RELATION_EXISTS);
        }

        LocalDateTime now = LocalDateTime.now();

        if (action == FollowRequestProcessRequest.ProcessStatus.ACCEPT) {
            request.accept(now);

            // Follow 생성 (requester -> target)
            followRepository.save(
                    new Follow(
                            request.getRequesterId(),
                            request.getTargetId(),
                            now
                    )
            );

            return FollowRequestProcessResponse.accepted(
                    request.getId(),
                    now,
                    request.getRequesterId(),
                    request.getTargetId()
            );
        }

        // REJECT
        request.reject(now);

        return FollowRequestProcessResponse.rejected(
                request.getId(),
                now,
                request.getRequesterId(),
                request.getTargetId()
        );
    }

    @Transactional(readOnly = true)
    public FollowRequestListResponse getMyPendingRequests(Long meUserId, int page, int size) {

        // page / size 검증
        if (page < 0 || size <= 0 || size > 50) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        PageRequest pageable = PageRequest.of(page, size);
        Page<FollowRequestListItemResponse> result =
                followRequestRepository.findMyPendingRequests(meUserId, pageable);

        return new FollowRequestListResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }
}
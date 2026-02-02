package com.kuit.chozy.postaction.service;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.postaction.domain.PostAction;
import com.kuit.chozy.postaction.domain.PostActionStatus;
import com.kuit.chozy.postaction.domain.PostActionType;
import com.kuit.chozy.postaction.dto.RepostCreateRequest;
import com.kuit.chozy.postaction.repository.PostActionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepostService {

    private final PostActionRepository postActionRepository;

    public RepostService(PostActionRepository postActionRepository) {
        this.postActionRepository = postActionRepository;
    }

    @Transactional
    public String repost(Long userId, RepostCreateRequest request) {

        ValidatedRepost validated = validateRequest(userId, request);

        Long postId = validated.postId;
        String hashTags = validated.hashTags;

        boolean quoteExists = postActionRepository.existsByPostIdAndUserIdAndTypeAndStatusNot(
                postId,
                userId,
                PostActionType.QUOTE,
                PostActionStatus.DELETED
        );

        if (quoteExists) {
            // TODO: 바뀐 에러코드로 교체
            throw new ApiException(ErrorCode.CANNOT_REPOST_WHEN_QUOTED);
        }

        boolean repostExists = postActionRepository.existsByPostIdAndUserIdAndTypeAndStatusNot(
                postId,
                userId,
                PostActionType.REPOST,
                PostActionStatus.DELETED
        );

        if (repostExists) {
            throw new ApiException(ErrorCode.REPOST_ALREADY_EXISTS);
        }

        PostAction action = PostAction.repost(
                postId,
                userId,
                toJsonString(hashTags)
        );

        try {
            postActionRepository.save(action);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.REPOST_ALREADY_EXISTS);
        }

        return "리포스트에 성공하였습니다.";
    }

    @Transactional
    public String cancelRepost(Long userId, Long feedId) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (feedId == null || feedId <= 0) {
            throw new ApiException(ErrorCode.INVALID_REPOST_REQUEST);
        }

        PostAction repost = postActionRepository.findByPostIdAndUserIdAndTypeAndStatusNot(
                feedId,
                userId,
                PostActionType.REPOST,
                PostActionStatus.DELETED
        ).orElseThrow(() -> new ApiException(ErrorCode.REPOST_NOT_FOUND));

        repost.delete();

        return "리포스트 취소에 성공하였습니다.";
    }

    private ValidatedRepost validateRequest(Long userId, RepostCreateRequest request) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REPOST_REQUEST);
        }

        if (request.getFeedId() == null || request.getFeedId() <= 0) {
            throw new ApiException(ErrorCode.INVALID_REPOST_REQUEST);
        }

        if (request.getHashTags() == null) {
            throw new ApiException(ErrorCode.INVALID_REPOST_REQUEST);
        }

        String normalizedHashTags = normalizeHashTags(request.getHashTags());

        if (!isValidHashtagFormat(normalizedHashTags) || normalizedHashTags.length() > 1000) {
            throw new ApiException(ErrorCode.INVALID_REPOST_REQUEST);
        }

        return new ValidatedRepost(request.getFeedId(), normalizedHashTags);
    }

    private String normalizeHashTags(String hashTags) {
        return hashTags.trim().replaceAll("\\s+", " ");
    }

    private boolean isValidHashtagFormat(String hashTags) {
        if (hashTags.isEmpty()) {
            return false;
        }

        String[] tokens = hashTags.split(" ");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            if (!token.startsWith("#")) return false;
            if (token.length() == 1) return false;
        }
        return true;
    }

    private String toJsonString(String hashTags) {
        String escaped = hashTags.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    private static class ValidatedRepost {
        private final Long postId;
        private final String hashTags;

        private ValidatedRepost(Long postId, String hashTags) {
            this.postId = postId;
            this.hashTags = hashTags;
        }
    }
}
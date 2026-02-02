package com.kuit.chozy.postaction.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.postaction.domain.PostAction;
import com.kuit.chozy.postaction.domain.PostActionStatus;
import com.kuit.chozy.postaction.domain.PostActionType;
import com.kuit.chozy.postaction.dto.QuoteCreateRequest;
import com.kuit.chozy.postaction.repository.PostActionRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuoteService {

    private final PostActionRepository postActionRepository;

    public QuoteService(PostActionRepository postActionRepository) {
        this.postActionRepository = postActionRepository;
    }

    @Transactional
    public String quote(Long userId, QuoteCreateRequest request) {

        validateRequest(userId, request);

        boolean exists = postActionRepository.existsByPostIdAndUserIdAndTypeAndStatusNot(
                request.getFeedId(),
                userId,
                PostActionType.QUOTE,
                PostActionStatus.DELETED
        );

        if (exists) {
            throw new ApiException(ErrorCode.QUOTE_ALREADY_EXISTS);
        }

        PostAction action = PostAction.quote(
                request.getFeedId(),
                userId,
                normalizeContent(request.getContent()),
                toJsonString(request.getHashTags())
        );

        try {
            postActionRepository.save(action);
        } catch (DataIntegrityViolationException e) {
            throw new ApiException(ErrorCode.QUOTE_ALREADY_EXISTS);
        }

        return "인용에 성공하였습니다.";
    }

    private void validateRequest(Long userId, QuoteCreateRequest request) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_QUOTE_REQUEST);
        }

        if (request.getFeedId() == null || request.getFeedId() <= 0) {
            throw new ApiException(ErrorCode.INVALID_QUOTE_REQUEST);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_QUOTE_REQUEST);
        }

        if (request.getContent().length() > 500) {
            throw new ApiException(ErrorCode.INVALID_QUOTE_REQUEST);
        }

        if (request.getHashTags() == null) {
            throw new ApiException(ErrorCode.INVALID_QUOTE_REQUEST);
        }

        String hashTags = normalizeHashTags(request.getHashTags());

        if (!isValidHashtagFormat(hashTags) || hashTags.length() > 1000) {
            throw new ApiException(ErrorCode.INVALID_QUOTE_REQUEST);
        }
    }

    private String normalizeContent(String content) {
        return content.trim();
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
        String escaped = hashTags
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }
}
package com.kuit.chozy.post.service;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.post.domain.Post;
import com.kuit.chozy.post.dto.PostCreateRequest;
import com.kuit.chozy.post.dto.PostUpdateRequest;
import com.kuit.chozy.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Transactional
    public String createPost(Long userId, PostCreateRequest request) {

        validateCreateRequest(userId, request);

        List<String> imageUrls = mapToImageKeys(request.getImg());

        Post post = Post.builder()
                .userId(userId)
                .content(normalizeContent(request.getContent()))
                .hashTags(normalizeHashTags(request.getHashTags()))
                .imageUrls(imageUrls)
                .build();

        postRepository.save(post);

        return "게시글을 성공적으로 게시했어요.";
    }

    @Transactional
    public String updatePost(Long userId, Long postId, PostUpdateRequest request) {

        validateUpdateRequest(userId, postId, request);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REQUEST_VALUE));

        if (!post.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        List<String> imageUrls = null;
        if (request.getImg() != null) {
            imageUrls = mapToImageKeys(request.getImg());
        }

        post.update(
                request.getContent() == null ? null : normalizeContent(request.getContent()),
                request.getHashTags() == null ? null : normalizeHashTags(request.getHashTags()),
                imageUrls
        );

        return "게시글을 성공적으로 수정했어요.";
    }

    private void validateCreateRequest(Long userId, PostCreateRequest request) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (request.getContent().length() > 5000) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (request.getHashTags() == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        String hashTags = normalizeHashTags(request.getHashTags());

        if (!isValidHashtagFormat(hashTags)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (hashTags.length() > 1000) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        validateImages(request.getImg());
    }

    private void validateUpdateRequest(Long userId, Long postId, PostUpdateRequest request) {

        if (userId == null || userId <= 0) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if (postId == null || postId <= 0) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (request == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (request.getContent() != null) {
            String content = request.getContent().trim();
            if (content.isEmpty() || content.length() > 5000) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }
        }

        if (request.getHashTags() != null) {
            String hashTags = normalizeHashTags(request.getHashTags());
            if (!isValidHashtagFormat(hashTags) || hashTags.length() > 1000) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }
        }

        validateImages(request.getImg());
    }

    private void validateImages(List<? extends Object> img) {
        if (img == null) {
            return;
        }

        if (img.size() > 10) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        for (Object obj : img) {
            if (obj == null) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }

            String fileName;
            String contentType;

            if (obj instanceof PostCreateRequest.ImageMeta meta) {
                fileName = meta.getFileName();
                contentType = meta.getContentType();
            } else if (obj instanceof PostUpdateRequest.ImageMeta meta) {
                fileName = meta.getFileName();
                contentType = meta.getContentType();
            } else {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }

            if (fileName == null || fileName.trim().isEmpty() || fileName.length() > 255) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }

            if (contentType == null || contentType.trim().isEmpty()) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }

            if (!isAllowedImageContentType(contentType.trim())) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }
        }
    }

    private List<String> mapToImageKeys(List<? extends Object> img) {
        if (img == null || img.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();

        for (Object obj : img) {
            if (obj instanceof PostCreateRequest.ImageMeta meta) {
                result.add(meta.getFileName().trim());
            } else if (obj instanceof PostUpdateRequest.ImageMeta meta) {
                result.add(meta.getFileName().trim());
            } else {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }
        }

        return result;
    }

    private boolean isAllowedImageContentType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
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
            if (token.isBlank()) {
                continue;
            }
            if (!token.startsWith("#")) {
                return false;
            }
            if (token.length() == 1) {
                return false;
            }
        }

        return true;
    }
}
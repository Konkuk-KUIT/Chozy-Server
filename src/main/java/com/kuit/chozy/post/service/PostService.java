package com.kuit.chozy.post.service;

import com.kuit.chozy.common.exception.ApiException;
import com.kuit.chozy.common.exception.ErrorCode;
import com.kuit.chozy.post.domain.Post;
import com.kuit.chozy.post.dto.PostCreateRequest;
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

        List<String> imageUrls = mapToImageUrls(request.getImg());

        Post post = Post.builder()
                .userId(userId)
                .content(request.getContent().trim())
                .tag(normalizeTag(request.getTag()))
                .imageUrls(imageUrls)
                .build();

        postRepository.save(post);

        return "게시글을 성공적으로 게시했어요.";
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

        if (request.getTag() == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        if (request.getTag().length() > 1000) {
            throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
        }

        List<PostCreateRequest.ImageMeta> img = request.getImg();
        if (img != null) {
            if (img.size() > 10) {
                throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
            }

            for (PostCreateRequest.ImageMeta meta : img) {
                if (meta == null) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
                }

                if (meta.getFileName() == null || meta.getFileName().trim().isEmpty()) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
                }

                if (meta.getFileName().length() > 255) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
                }

                if (meta.getContentType() == null || meta.getContentType().trim().isEmpty()) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
                }

                if (!isAllowedImageContentType(meta.getContentType().trim())) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST_VALUE);
                }
            }
        }
    }

    private List<String> mapToImageUrls(List<PostCreateRequest.ImageMeta> img) {
        if (img == null || img.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (PostCreateRequest.ImageMeta meta : img) {
            result.add(meta.getFileName().trim());
        }
        return result;
    }

    private boolean isAllowedImageContentType(String contentType) {
        return "image/jpeg".equals(contentType)
                || "image/png".equals(contentType)
                || "image/webp".equals(contentType);
    }

    private String normalizeTag(String tag) {
        // 입력 그대로 저장할지, 공백 정리만 할지 정책 선택
        // 지금은 양 끝 공백만 정리해서 저장
        return tag.trim();
    }
}
package com.kuit.chozy.post.controller;

import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.post.dto.PostCreateRequest;
import com.kuit.chozy.post.dto.PostUpdateRequest;
import com.kuit.chozy.post.service.PostService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/community/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/create")
    public ApiResponse<String> create(@RequestBody PostCreateRequest request) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        String result = postService.createPost(meId, request);
        return ApiResponse.success(result);
    }

    @PatchMapping("/{postId}")
    public ApiResponse<String> update(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {

        // TODO: accessToken 기반으로 meId 추출
        Long meId = 1L;

        String result = postService.updatePost(meId, postId, request);
        return ApiResponse.success(result);
    }
}
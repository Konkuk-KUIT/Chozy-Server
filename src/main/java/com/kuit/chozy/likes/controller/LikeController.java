package com.kuit.chozy.likes.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.likes.dto.request.LikeListRequest;
import com.kuit.chozy.likes.dto.request.LikeRequest;
import com.kuit.chozy.likes.dto.response.LikeItemResponse;
import com.kuit.chozy.likes.dto.response.LikeListResult;
import com.kuit.chozy.likes.entity.ProductFavorite;
import com.kuit.chozy.likes.service.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/likes")
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public ApiResponse<Void> likeProduct(
            @UserId Long userId,
            @RequestBody @Valid LikeRequest request
    ) {
        return ApiResponse.success(likeService.likeProduct(userId, request.productId(), request.like()));
    }

    @GetMapping
    public ApiResponse<LikeListResult> getLikeProducts(
            @UserId Long userId,
            @ModelAttribute LikeListRequest request
    ){
        int page = request.pageOrDefault();
        int size = request.sizeOrDefault();
        return ApiResponse.success(likeService.getLikeProducts(userId, request.search(), page, size));
    }

}

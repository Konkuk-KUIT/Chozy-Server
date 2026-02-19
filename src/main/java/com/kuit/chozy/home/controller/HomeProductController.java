package com.kuit.chozy.home.controller;

import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.home.dto.request.HomeProductsRequest;
import com.kuit.chozy.home.dto.request.HomeRecommendProductsRequest;
import com.kuit.chozy.home.dto.request.SaveRecentProductRequest;
import com.kuit.chozy.home.dto.response.HomeProductItemResponse;
import com.kuit.chozy.home.dto.response.HomeProductsResult;
import com.kuit.chozy.home.service.HomeProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/home/products")
@RequiredArgsConstructor
public class HomeProductController {
    private final HomeProductService homeProductService;

    // 상품 목록 조회
    @GetMapping
    public ApiResponse<HomeProductsResult> getHomeProducts(
            @UserId(required = false) Long userId,
            @ModelAttribute HomeProductsRequest request
        ){
        return ApiResponse.success(homeProductService.getHomeProducts(userId, request));
    }

    // 최근 본 상품 저장
    @Operation(security = { @SecurityRequirement(name = "BearerAuth") })
    @PostMapping("/recent")
    public ApiResponse<Void> saveRecentProduct(
            @UserId Long userId,
            @RequestBody SaveRecentProductRequest request
    ){
        return ApiResponse.success(homeProductService.saveRecentProduct(userId, request.productId(), false));
    }

    // 최근 본 상품 목록 조회
    @Operation(security = { @SecurityRequirement(name = "BearerAuth") })
    @GetMapping("/recent")
    public ApiResponse<List<HomeProductItemResponse>> getRecentProducts(
            @UserId Long userId
    ){
        return ApiResponse.success(homeProductService.getRecentProducts(userId));
    }

    // 이런 상품은 어떠세요? 조회
    @GetMapping("/recommend")
    public ApiResponse<HomeProductsResult> getHomeRecommendedProducts(
            @UserId(required = false) Long userId,
            @ModelAttribute HomeRecommendProductsRequest request
    ){
        int page = request.pageOrDefault();
        int size = Math.min(request.sizeOrDefault(), 50);
        return ApiResponse.success(homeProductService.getHomeRecommendedProducts(userId, page, size));
    }
}

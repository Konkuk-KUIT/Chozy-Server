package com.kuit.chozy.auth.controller;

import com.kuit.chozy.auth.dto.response.KakaoLoginResponse;
import com.kuit.chozy.auth.dto.response.OAuthLoginResponse;
import com.kuit.chozy.auth.service.KakaoAuthService;
import com.kuit.chozy.auth.service.NaverAuthService;
import com.kuit.chozy.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;

    @Operation(summary = "카카오 로그인 콜백", security = {})
    @GetMapping("/auth/kakao/callback")
    public ApiResponse<KakaoLoginResponse> kakaoCallback(
            @RequestParam("code") String code
    ) {
        KakaoLoginResponse result =
                kakaoAuthService.loginWithAuthorizationCode(code);

        return ApiResponse.success(result);
    }

    @Operation(summary = "네이버 로그인 콜백", security = {})
    @GetMapping("/auth/naver/callback")
    public ApiResponse<OAuthLoginResponse> naverCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state
    ) {
        OAuthLoginResponse result =
                naverAuthService.loginWithAuthorizationCode(code, state);

        return ApiResponse.success(result);
    }
}

package com.kuit.chozy.auth.controller;

import com.kuit.chozy.auth.dto.response.KakaoLoginResponse;
import com.kuit.chozy.auth.service.KakaoAuthService;
import com.kuit.chozy.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/auth/kakao/callback")
    public ApiResponse<KakaoLoginResponse> kakaoCallback(
            @RequestParam("code") String code
    ) {
        KakaoLoginResponse result =
                kakaoAuthService.loginWithAuthorizationCode(code);

        return ApiResponse.success(result);
    }
}
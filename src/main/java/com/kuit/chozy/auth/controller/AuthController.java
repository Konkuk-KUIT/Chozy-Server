package com.kuit.chozy.auth.controller;

import com.kuit.chozy.auth.dto.request.LoginRequest;
import com.kuit.chozy.auth.dto.request.RefreshTokenRequest;
import com.kuit.chozy.auth.dto.request.SignupRequest;
import com.kuit.chozy.auth.dto.response.*;
import com.kuit.chozy.auth.service.AuthService;
import com.kuit.chozy.global.common.auth.UserId;
import com.kuit.chozy.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ApiResponse.success(authService.login(request));
    }

    // 회원가입
    @PostMapping("/signup")
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request){
        return ApiResponse.success(authService.signup(request));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@UserId Long userId){
        return ApiResponse.success(authService.logout(userId));
    }

    // 엑세스 토큰 재발급
    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request){
        return ApiResponse.success(authService.refreshToken(request));
    }

    // 회원 탈퇴
    @PostMapping("/withdraw")
    public ApiResponse<WithdrawResponse> withdraw(@UserId Long userId){
        return ApiResponse.success(authService.withdraw(userId));
    }

    // 아이디 중복 검사
    @GetMapping("/check-id")
    public ApiResponse<LoginIdCheckResponse> checkLoginId(
            @RequestParam @NotBlank String loginId
    ){
        return ApiResponse.success(authService.checkLoginId(loginId));
    }
}

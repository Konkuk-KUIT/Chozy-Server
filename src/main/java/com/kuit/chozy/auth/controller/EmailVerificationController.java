package com.kuit.chozy.auth.controller;

import com.kuit.chozy.auth.dto.request.SendEmailCodeRequest;
import com.kuit.chozy.auth.dto.request.VerifyEmailCodeRequest;
import com.kuit.chozy.auth.service.EmailVerificationService;
import com.kuit.chozy.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/auth/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    // 인증번호 발송
    @Operation(summary = "이메일 인증번호 발송", security = {})
    @PostMapping("/verification-code")
    public ApiResponse<Void> sendCode(
            @Valid @RequestBody SendEmailCodeRequest request
    ){
        emailVerificationService.sendCode(request.email());
        return ApiResponse.success(null);
    }

    // 인증번호 검증
    @Operation(summary = "이메일 인증번호 검증", security = {})
    @PostMapping("/verify")
    public ApiResponse<Void> verifyCode(
            @Valid @RequestBody VerifyEmailCodeRequest request
    ){
        emailVerificationService.verifyCode(request.email(), request.code());
        return ApiResponse.success(null);
    }
}

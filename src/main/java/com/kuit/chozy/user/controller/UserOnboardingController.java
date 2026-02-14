package com.kuit.chozy.user.controller;

import com.kuit.chozy.global.common.response.ApiResponse;
import com.kuit.chozy.user.dto.request.OnboardingNicknameRequest;
import com.kuit.chozy.user.service.UserOnboardingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me")
public class UserOnboardingController {

    private final UserOnboardingService userOnboardingService;

    @PatchMapping("/onboarding")
    public ApiResponse<Void> onboarding(@RequestHeader("Authorization") String authorization,
                                        @RequestBody @Valid OnboardingNicknameRequest request) {
        userOnboardingService.updateNickname(authorization, request.nickname());
        return ApiResponse.success(null);
    }
}
package com.kuit.chozy.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OnboardingNicknameRequest(
        @NotBlank String nickname
) {}
package com.kuit.chozy.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyEmailCodeRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min=6, max=6)
        String code
) {
}

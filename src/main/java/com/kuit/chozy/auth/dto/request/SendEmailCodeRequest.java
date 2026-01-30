package com.kuit.chozy.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendEmailCodeRequest(
        @NotBlank
        @Email
        String email
) {
}

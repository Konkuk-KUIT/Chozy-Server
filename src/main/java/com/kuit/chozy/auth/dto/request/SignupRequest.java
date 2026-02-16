package com.kuit.chozy.auth.dto.request;

import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank
        String loginId,

        @NotBlank
        @Size(min=8, max=16)
        String password,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^[가-힣]+( [가-힣]+)*$",
                message = "닉네임은 한글과 단일 공백만 사용할 수 있습니다."
        )
        String nickname

) {
}

package com.kuit.chozy.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn
) {
    public static LoginResponse of(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn
    ) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiresIn,
                refreshTokenExpiresIn
        );
    }
}

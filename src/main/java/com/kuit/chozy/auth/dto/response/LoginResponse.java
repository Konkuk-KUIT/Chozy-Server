package com.kuit.chozy.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean needsOnboarding
) {
    public static LoginResponse of(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn,
            boolean needsOnboarding
    ) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                accessTokenExpiresIn,
                refreshTokenExpiresIn,
                needsOnboarding
        );
    }
}

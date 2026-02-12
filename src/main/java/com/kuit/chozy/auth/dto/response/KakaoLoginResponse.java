package com.kuit.chozy.auth.dto.response;

public record KakaoLoginResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean needsOnboarding,
        boolean isNewUser
) {
    public static KakaoLoginResponse of(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn,
            boolean needsOnboarding,
            boolean isNewUser
    ) {
        return new KakaoLoginResponse(
                accessToken,
                refreshToken,
                accessTokenExpiresIn,
                refreshTokenExpiresIn,
                needsOnboarding,
                isNewUser
        );
    }
}
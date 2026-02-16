package com.kuit.chozy.auth.dto.response;

public record OAuthLoginResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresIn,
        long refreshTokenExpiresIn,
        boolean needsOnboarding,
        boolean isNewUser
) {
    public static OAuthLoginResponse of(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresIn,
            long refreshTokenExpiresIn,
            boolean needsOnboarding,
            boolean isNewUser
    ) {
        return new OAuthLoginResponse(
                accessToken,
                refreshToken,
                accessTokenExpiresIn,
                refreshTokenExpiresIn,
                needsOnboarding,
                isNewUser
        );
    }
}

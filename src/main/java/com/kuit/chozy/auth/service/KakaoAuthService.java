package com.kuit.chozy.auth.service;

import com.kuit.chozy.auth.dto.response.KakaoLoginResponse;
import com.kuit.chozy.auth.entity.AuthProvider;
import com.kuit.chozy.auth.entity.TokenStatus;
import com.kuit.chozy.auth.entity.UserAuth;
import com.kuit.chozy.auth.entity.UserToken;
import com.kuit.chozy.auth.repository.UserAuthRepository;
import com.kuit.chozy.auth.repository.UserTokenRepository;
import com.kuit.chozy.global.common.config.OAuthConfig;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public KakaoLoginResponse loginWithAuthorizationCode(String code) {

        String kakaoAccessToken = kakaoApiClient.getAccessToken(code);
        String kakaoId = kakaoApiClient.getKakaoId(kakaoAccessToken);

        UserAuth kakaoAuth = userAuthRepository
                .findByProviderAndProviderUserId(AuthProvider.KAKAO, kakaoId)
                .orElse(null);

        User user;
        boolean isNewUser = false;

        if (kakaoAuth == null) {

            user = User.builder()
                    .status(UserStatus.ACTIVE)
                    .build();

            user = userRepository.save(user);

            UserAuth newAuth =
                    UserAuth.createKakao(user, kakaoId);

            userAuthRepository.save(newAuth);

            isNewUser = true;
        } else {
            user = kakaoAuth.getUser();
        }

        Long userId = user.getId();

        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        userTokenRepository.findByUserIdAndStatus(userId, TokenStatus.ACTIVE)
                .ifPresent(UserToken::inactivate);

        userTokenRepository.save(UserToken.create(userId, refreshToken));

        boolean needsOnboarding =
                (user.getNickname() == null || user.getNickname().isBlank());

        return KakaoLoginResponse.of(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiresInSeconds(),
                jwtUtil.getRefreshTokenExpiresInSeconds(),
                needsOnboarding,
                isNewUser
        );
    }
}

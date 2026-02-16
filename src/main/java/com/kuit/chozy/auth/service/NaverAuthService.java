package com.kuit.chozy.auth.service;

import com.kuit.chozy.auth.dto.response.OAuthLoginResponse;
import com.kuit.chozy.auth.entity.AuthProvider;
import com.kuit.chozy.auth.entity.TokenStatus;
import com.kuit.chozy.auth.entity.UserAuth;
import com.kuit.chozy.auth.entity.UserToken;
import com.kuit.chozy.auth.repository.UserAuthRepository;
import com.kuit.chozy.auth.repository.UserTokenRepository;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NaverAuthService {

    private final NaverApiClient naverApiClient;
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public OAuthLoginResponse loginWithAuthorizationCode(String code, String state) {
        String naverAccessToken = naverApiClient.getAccessToken(code, state);
        String naverId = naverApiClient.getNaverId(naverAccessToken);

        UserAuth naverAuth = userAuthRepository
                .findByProviderAndProviderUserId(AuthProvider.NAVER, naverId)
                .orElse(null);

        User user;
        boolean isNewUser = false;

        if (naverAuth == null) {
            user = User.builder()
                    .status(UserStatus.ACTIVE)
                    .build();

            user = userRepository.save(user);

            UserAuth newAuth = UserAuth.createNaver(user, naverId);
            userAuthRepository.save(newAuth);

            isNewUser = true;
        } else {
            user = naverAuth.getUser();
        }

        Long userId = user.getId();

        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        userTokenRepository.findByUserIdAndStatus(userId, TokenStatus.ACTIVE)
                .ifPresent(UserToken::inactivate);

        userTokenRepository.save(UserToken.create(userId, refreshToken));

        boolean needsOnboarding =
                (user.getNickname() == null || user.getNickname().isBlank());

        return OAuthLoginResponse.of(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiresInSeconds(),
                jwtUtil.getRefreshTokenExpiresInSeconds(),
                needsOnboarding,
                isNewUser
        );
    }
}

package com.kuit.chozy.auth.service;

import com.kuit.chozy.auth.dto.request.LoginRequest;
import com.kuit.chozy.auth.dto.request.RefreshTokenRequest;
import com.kuit.chozy.auth.dto.request.SignupRequest;
import com.kuit.chozy.auth.dto.response.*;
import com.kuit.chozy.auth.entity.AuthProvider;
import com.kuit.chozy.auth.entity.TokenStatus;
import com.kuit.chozy.auth.entity.UserAuth;
import com.kuit.chozy.auth.entity.UserToken;
import com.kuit.chozy.auth.repository.UserAuthRepository;
import com.kuit.chozy.auth.repository.UserTokenRepository;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenRepository userTokenRepository;

    @Transactional
    public LoginResponse login(@Valid LoginRequest request) {
        UserAuth userAuth = userAuthRepository
                .findByProviderAndLoginId(AuthProvider.LOCAL, request.loginId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        User user = userAuth.getUser();

        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.password(), userAuth.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        Long userId = user.getId();

        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        userTokenRepository.findByUserIdAndStatus(userId, TokenStatus.ACTIVE)
                .ifPresent(UserToken::inactivate);

        userTokenRepository.save(UserToken.create(userId, refreshToken));

        boolean needsOnboarding =
                (user.getNickname() == null || user.getNickname().isBlank());

        return LoginResponse.of(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiresInSeconds(),
                jwtUtil.getRefreshTokenExpiresInSeconds(),
                needsOnboarding
        );
    }

    @Transactional
    public Void signup(@Valid SignupRequest request) {
        emailVerificationService.assertVerified(request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userAuthRepository.existsByProviderAndLoginId(AuthProvider.LOCAL, request.loginId())) {
            throw new ApiException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .status(UserStatus.ACTIVE)
                .build();

        user = userRepository.save(user);

        UserAuth localAuth = UserAuth.createLocal(user, request.loginId(), encodedPassword);
        userAuthRepository.save(localAuth);

        return null;
    }

    @Transactional
    public LogoutResponse logout(Long userId) {
        userTokenRepository.findByUserIdAndStatus(userId, TokenStatus.ACTIVE)
                .ifPresent(UserToken::inactivate);
        return LogoutResponse.success();
    }

    @Transactional
    public RefreshTokenResponse refreshToken(@Valid RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        Long userId;
        try {
            userId = jwtUtil.getUserId(refreshToken);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        UserToken token = userTokenRepository.findByRefreshTokenAndStatus(refreshToken, TokenStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REFRESH_TOKEN));

        /* TODO: token.getId() vs token.getUserId() 확인 필요 */
        if (!token.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        return new RefreshTokenResponse(newAccessToken);
    }

    @Transactional
    public WithdrawResponse withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<UserToken> tokens = userTokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
        tokens.forEach(UserToken::inactivate);

        user.delete();
        return WithdrawResponse.success();
    }

    public LoginIdCheckResponse checkLoginId(@NotBlank String loginId) {
        boolean exists = userAuthRepository.existsByProviderAndLoginId(AuthProvider.LOCAL, loginId);
        return new LoginIdCheckResponse(!exists);
    }
}
package com.kuit.chozy.auth.service;


import com.kuit.chozy.auth.dto.request.RefreshTokenRequest;
import com.kuit.chozy.auth.dto.request.SignupRequest;
import com.kuit.chozy.auth.dto.response.*;
import com.kuit.chozy.auth.entity.TokenStatus;
import com.kuit.chozy.auth.entity.UserToken;
import com.kuit.chozy.auth.repository.UserTokenRepository;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.auth.dto.request.LoginRequest;
import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
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
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenRepository userTokenRepository;

    @Transactional
    public LoginResponse login(@Valid LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        Long userId = user.getId();

        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        // 기존 토큰 무효화
        userTokenRepository.findByUserIdAndStatus(userId, TokenStatus.ACTIVE)
                .ifPresent(UserToken::inactivate);

        // 새 리프레시 토큰 저장
        userTokenRepository.save(UserToken.create(userId, refreshToken));

        return LoginResponse.of(
                accessToken,
                refreshToken,
                jwtUtil.getAccessTokenExpiresInSeconds(),
                jwtUtil.getRefreshTokenExpiresInSeconds()
        );
    }

    @Transactional
    public Void signup(@Valid SignupRequest request) {
        // 이메일 검증 확인
        emailVerificationService.assertVerified(request.email());

        // 중복 체크
        if(userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.DUPLICATE_EMAIL);
        }
        if(userRepository.existsByLoginId(request.loginId())) {
            throw new ApiException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 유저 저장
        User user = User.builder().
                loginId(request.loginId()).
                password(encodedPassword).
                email(request.email()).
                nickname(request.nickname()).
                status(UserStatus.ACTIVE).
        build();
        userRepository.save(user);

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

        if (token.getId().equals(userId)) {
            throw new ApiException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtUtil.generateAccessToken(userId);
        return new RefreshTokenResponse(newAccessToken);
    }

    @Transactional
    public WithdrawResponse withdraw(Long userId) {
        User user =  userRepository.findById(userId).orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<UserToken> tokens = userTokenRepository.findAllByUserIdAndStatus(userId, TokenStatus.ACTIVE);
        tokens.forEach(UserToken::inactivate);

        user.delete();
        return WithdrawResponse.success();
    }

    public LoginIdCheckResponse checkLoginId(@NotBlank String loginId) {
        boolean exists = userRepository.existsByLoginId(loginId);
        return new LoginIdCheckResponse(!exists);
    }
}

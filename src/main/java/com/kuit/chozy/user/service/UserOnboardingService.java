package com.kuit.chozy.user.service;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import com.kuit.chozy.global.jwt.JwtUtil;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserOnboardingService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserOnboardingService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public void updateNickname(String authorizationHeader, String nickname) {
        Long userId = extractUserId(authorizationHeader);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (UserStatus.DELETED.equals(user.getStatus())) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        /* 닉네임 정책: 중복 허용, 형식만 검증 */
        validateNickname(nickname);

        user.setNickname(nickname);
    }

    private Long extractUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        String token = authorizationHeader.substring(7);
        try {
            return jwtUtil.getUserId(token);
        } catch (Exception e) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void validateNickname(String nickname) {
        if (nickname == null) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }

        if (nickname.length() > 8) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }

        if (!nickname.matches("^[가-힣 ]*$")) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
    }
}
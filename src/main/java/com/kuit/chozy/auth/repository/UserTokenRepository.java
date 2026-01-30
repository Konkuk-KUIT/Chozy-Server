package com.kuit.chozy.auth.repository;

import com.kuit.chozy.auth.entity.TokenStatus;
import com.kuit.chozy.auth.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
    Optional<UserToken> findByUserIdAndStatus(Long userId, TokenStatus status);
    Optional<UserToken> findByRefreshTokenAndStatus(String refreshToken, TokenStatus status);
    List<UserToken> findAllByUserIdAndStatus(Long userId, TokenStatus status);
}

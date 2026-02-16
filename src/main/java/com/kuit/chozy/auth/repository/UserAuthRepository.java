package com.kuit.chozy.auth.repository;

import com.kuit.chozy.auth.entity.AuthProvider;
import com.kuit.chozy.auth.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    Optional<UserAuth> findByProviderAndLoginId(AuthProvider provider, String loginId);

    Optional<UserAuth> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    boolean existsByProviderAndLoginId(AuthProvider provider, String loginId);

    boolean existsByProviderAndProviderUserId(AuthProvider provider, String providerUserId);
}
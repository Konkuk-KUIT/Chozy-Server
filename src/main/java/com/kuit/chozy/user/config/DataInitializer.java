package com.kuit.chozy.user.config;

import com.kuit.chozy.auth.entity.AuthProvider;
import com.kuit.chozy.auth.entity.UserAuth;
import com.kuit.chozy.auth.repository.UserAuthRepository;
import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        String adminLoginId = "admin";
        String rawAdminPassword = "admin123";

        // 1) user_auth 기준으로 admin 유저 찾기 (LOCAL + loginId)
        UserAuth adminAuth = userAuthRepository
                .findByProviderAndLoginId(AuthProvider.LOCAL, adminLoginId)
                .orElse(null);

        User admin;

        if (adminAuth == null) {
            // 2) 없으면 users 먼저 생성 (users.loginId/password는 세팅하지 않음)
            admin = User.builder()
                    .email("admin@chozy.com")
                    .nickname("admin")
                    .status(UserStatus.ACTIVE)
                    .build();

            admin = userRepository.save(admin);

            // 3) user_auth 생성 (비밀번호는 해시 저장)
            String encoded = passwordEncoder.encode(rawAdminPassword);
            adminAuth = UserAuth.createLocal(admin, adminLoginId, encoded);
            userAuthRepository.save(adminAuth);

        } else {
            admin = adminAuth.getUser();

            // 4) 기존 adminAuth 있으면 비밀번호 해시 갱신(기존 정책 유지)
            String encoded = passwordEncoder.encode(rawAdminPassword);
            adminAuth.updatePasswordHash(encoded);
            userAuthRepository.save(adminAuth);
        }

        // 5) users 프로필 필드 upsert
        admin.setEmail("admin@chozy.com");
        admin.setName("Admin");
        admin.setNickname("admin");
        admin.setPhoneNumber("010-9475-0679");
        admin.setStatusMessage("관리자 계정입니다");
        admin.setCountry("KOR");
        admin.setBirthDate(LocalDate.of(2002, 10, 4));
        admin.setHeight(163f);
        admin.setWeight(52f);
        admin.setProfileImageUrl("profile");
        admin.setBackgroundImageUrl("bg");
        admin.setAccountPublic(true);
        admin.setBirthPublic(false);
        admin.setHeightPublic(false);
        admin.setWeightPublic(false);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setPasswordUpdatedAt(LocalDateTime.now());

        userRepository.save(admin);

        log.info("Admin user initialized (upsert, user_auth 기반): {}", adminLoginId);
    }
}
package com.kuit.chozy.user.config;

import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        initializeAdminUser();
    }

    private void initializeAdminUser() {
        String adminLoginId = "admin";
        
        if (userRepository.findByLoginId(adminLoginId).isEmpty()) {
            User adminUser = User.builder()
                    .loginId(adminLoginId)
                    .password("admin123")
                    .email("admin@chozy.com")
                    .name("Admin")
                    .nickname("admin")
                    .phoneNumber(null)
                    .statusMessage("관리자 계정입니다")
                    .country("Korea")
                    .birthDate(null)
                    .height(0.0f)
                    .weight(0.0f)
                    .profileImageUrl(null)
                    .isAccountPublic(true)
                    .isBirthPublic(false)
                    .isHeightPublic(false)
                    .isWeightPublic(false)
                    .status(UserStatus.ACTIVE)
                    .passwordUpdatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(adminUser);
            log.info("Admin user created successfully with loginId: {}", adminLoginId);
        } else {
            log.info("Admin user already exists with loginId: {}", adminLoginId);
        }
    }
}

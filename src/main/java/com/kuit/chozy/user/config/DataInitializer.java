package com.kuit.chozy.user.config;

import com.kuit.chozy.user.domain.User;
import com.kuit.chozy.user.domain.UserStatus;
import com.kuit.chozy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
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

        User admin = userRepository.findByLoginId(adminLoginId)
                .orElseGet(() -> User.builder()
                        .loginId(adminLoginId)
                        .build()
                );

        admin.setPassword("admin123");
        admin.setEmail("admin@chozy.com");
        admin.setName("Admin");
        admin.setNickname("admin");
        admin.setPhoneNumber("010-9475-0679");
        admin.setStatusMessage("관리자 계정입니다");
        admin.setCountry("KOR");
        admin.setBirthDate(LocalDate.of(2002,10,4));
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

        log.info("Admin user initialized (upsert): {}", adminLoginId);
    }
}

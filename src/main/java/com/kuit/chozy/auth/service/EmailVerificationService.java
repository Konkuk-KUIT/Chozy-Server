package com.kuit.chozy.auth.service;

import com.kuit.chozy.global.common.exception.ApiException;
import com.kuit.chozy.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationRedisRepository redisRepository;
    private final MailService mailService;

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(30);

    public void sendCode(String email){
        String code = generate6DigitCode();

        // redis에 저장
        redisRepository.saveCode(email, code, CODE_TTL);

        // 인증코드 전송
        mailService.sendEmailVerificationCode(email, code);
    }

    public void verifyCode(String email, String code){
        String saveCode = redisRepository.getCode(email);

        if(saveCode == null){
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        if(!saveCode.equals(code)){
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        redisRepository.deleteCode(email);
        redisRepository.markVerified(email,  VERIFIED_TTL);
    }

    public void assertVerified(String email){
        if(!redisRepository.isVerified(email)){
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    }

    private String generate6DigitCode() {
        int n = java.util.concurrent.ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%06d", n);
    }

}

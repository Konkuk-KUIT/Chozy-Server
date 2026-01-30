package com.kuit.chozy.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmailVerificationCode(String email,  String code){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[Chozy] 이메일 인증번호 안내");
        message.setText("""
                안녕하세요.
                
                이메일 인증번호는 아래와 같습니다.
    
                인증번호: %s
    
                ※ 인증번호는 5분간 유효합니다.
                """.formatted(code));

        javaMailSender.send(message);
    }
}

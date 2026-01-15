package com.kuit.chozy.me.dto.response;

import com.kuit.chozy.user.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ProfileResponseDto {

    private String loginId;
    private String nickname;
    private String profileImageUrl;
    private String statusMessage;

    private boolean isAccountPublic;

    private LocalDate birthDate;
    private float height;
    private float weight;

    private boolean isBirthPublic;
    private boolean isHeightPublic;
    private boolean isWeightPublic;

    public static ProfileResponseDto from(User user) {
        return ProfileResponseDto.builder()
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .statusMessage(user.getStatusMessage())
                .isAccountPublic(user.isAccountPublic())
                .birthDate(user.getBirthDate())
                .height(user.getHeight())
                .weight(user.getWeight())
                .isBirthPublic(user.isBirthPublic())
                .isHeightPublic(user.isHeightPublic())
                .isWeightPublic(user.isWeightPublic())
                .build();
    }
}

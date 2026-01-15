package com.kuit.chozy.me.dto.request;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class ProfileUpdateDto {

    private String nickname;
    private String statusMessage;
    private String profileImageUrl;

    private Boolean isAccountPublic;

    private LocalDate birthDate;
    private Float height;
    private Float weight;

    private Boolean isBirthPublic;
    private Boolean isHeightPublic;
    private Boolean isWeightPublic;
}
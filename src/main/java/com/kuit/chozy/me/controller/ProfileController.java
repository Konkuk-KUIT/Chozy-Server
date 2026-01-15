package com.kuit.chozy.me.controller;

import com.kuit.chozy.common.response.ApiResponse;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/me/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ApiResponse<ProfileResponseDto> getMyProfile(
            String loginId
    ) {
        return ApiResponse.success(
                profileService.getMyProfile(loginId)
        );
    }

    @PatchMapping
    public ApiResponse<ProfileResponseDto> updateMyProfile(
            String loginId,
            @RequestBody ProfileUpdateDto request
    ) {
        return ApiResponse.success(
                profileService.updateMyProfile(loginId, request)
        );
    }
}
package com.kuit.chozy.me.service;

import com.kuit.chozy.me.dto.request.ProfileUpdateDto;
import com.kuit.chozy.me.dto.response.ProfileResponseDto;
import com.kuit.chozy.user.domain.User;

import com.kuit.chozy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponseDto getMyProfile(String loginId) {
        User user = getActiveUser(loginId);
        return ProfileResponseDto.from(user);
    }

    @Transactional
    public ProfileResponseDto updateMyProfile(
            String loginId,
            ProfileUpdateDto request
    ) {
        User user = getActiveUser(loginId);

        if (request.getNickname() != null)
            user.setNickname(request.getNickname());

        if (request.getStatusMessage() != null)
            user.setStatusMessage(request.getStatusMessage());

        if (request.getProfileImageUrl() != null)
            user.setProfileImageUrl(request.getProfileImageUrl());

        if (request.getIsAccountPublic() != null)
            user.setAccountPublic(request.getIsAccountPublic());

        if (request.getBirthDate() != null)
            user.setBirthDate(request.getBirthDate());

        if (request.getHeight() != null)
            user.setHeight(request.getHeight());

        if (request.getWeight() != null)
            user.setWeight(request.getWeight());

        if (request.getIsBirthPublic() != null)
            user.setBirthPublic(request.getIsBirthPublic());

        if (request.getIsHeightPublic() != null)
            user.setHeightPublic(request.getIsHeightPublic());

        if (request.getIsWeightPublic() != null)
            user.setWeightPublic(request.getIsWeightPublic());

        return ProfileResponseDto.from(user);
    }

    private User getActiveUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        if (!user.isActive()) {
            throw new RuntimeException("USER_INACTIVE");
        }

        return user;
    }
}
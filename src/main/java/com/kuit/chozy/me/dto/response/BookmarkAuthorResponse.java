package com.kuit.chozy.me.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookmarkAuthorResponse {
    private final Long userId;
    private final String loginId;
    private final String nickname;
    private final String profileImageUrl;
}

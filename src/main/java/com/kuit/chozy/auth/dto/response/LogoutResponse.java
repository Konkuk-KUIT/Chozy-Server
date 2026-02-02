package com.kuit.chozy.auth.dto.response;

import java.time.LocalDateTime;

public record LogoutResponse(
        Boolean loggedOut,
        LocalDateTime logoutAt
) {
    public static LogoutResponse success() {
        return new LogoutResponse(true, LocalDateTime.now());
    }
}

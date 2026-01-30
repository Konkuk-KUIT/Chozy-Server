package com.kuit.chozy.auth.dto.response;

import java.time.LocalDateTime;

public record WithdrawResponse(
        boolean withdrawn,
        LocalDateTime withdrawnAt
) {
    public static WithdrawResponse success() {
        return new WithdrawResponse(true, LocalDateTime.now());
    }
}

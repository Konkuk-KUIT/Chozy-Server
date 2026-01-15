package com.kuit.chozy.common.response;

import java.time.LocalDateTime;

public class ErrorResponse {

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponse(int code, String message) {
        this.isSuccess = false;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
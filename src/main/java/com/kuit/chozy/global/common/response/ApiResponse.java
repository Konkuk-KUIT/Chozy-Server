package com.kuit.chozy.global.common.response;

import java.time.LocalDateTime;

public class ApiResponse<T> {

    private final boolean isSuccess;
    private final int code;
    private final String message;
    private final LocalDateTime timestamp;
    private final T result;

    private ApiResponse(boolean isSuccess, int code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.result = result;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(
                true,
                1000,
                "요청에 성공하였습니다.",
                result
        );
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

    public T getResult() {
        return result;
    }
}

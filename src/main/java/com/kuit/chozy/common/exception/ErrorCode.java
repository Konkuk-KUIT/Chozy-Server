package com.kuit.chozy.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4003, "자기 자신은 팔로우할 수 없습니다."),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4012, "인증이 필요합니다."),

    // 403
    DEACTIVATED_ACCOUNT(HttpStatus.FORBIDDEN, 4030, "비활성화된 계정입니다."),

    // 404
    TARGET_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4041, "대상 사용자를 찾을 수 없습니다."),

    // 409
    CANNOT_FOLLOW_BLOCKED_USER(HttpStatus.CONFLICT, 4095, "차단한 사용자는 팔로우할 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, 4096, "이미 팔로우한 사용자입니다."),
    ALREADY_REQUESTED(HttpStatus.CONFLICT, 4098, "이미 팔로우 요청을 보낸 사용자입니다."),

    // 500
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

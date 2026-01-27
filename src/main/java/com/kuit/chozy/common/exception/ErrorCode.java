package com.kuit.chozy.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4003, "자기 자신은 팔로우할 수 없습니다."),
    INVALID_REQUEST(org.springframework.http.HttpStatus.BAD_REQUEST, 4001, "요청 값이 올바르지 않습니다."),
    SELF_BLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4004, "자기 자신은 차단할 수 없습니다."),
    SELF_UNBLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4006, "자기 자신에 대한 차단은 해제할 수 없습니다."),
    ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, 4005, "이미 차단한 사용자입니다."),
    SELF_CLOSE_FRIEND_NOT_ALLOWED(HttpStatus.BAD_REQUEST,4007, "자기 자신은 친한 친구로 설정할 수 없습니다."),
    INVALID_REQUEST_VALUE(HttpStatus.BAD_REQUEST,4001, "요청 값이 올바르지 않습니다."),
    INVALID_REVIEW_REQUEST(HttpStatus.BAD_REQUEST, 4010, "리뷰 요청 값이 올바르지 않습니다."),

    // 401
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4012, "인증이 필요합니다."),

    // 403
    DEACTIVATED_ACCOUNT(HttpStatus.FORBIDDEN, 4030, "비활성화된 계정입니다."),
    FOLLOW_REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN,4032, "해당 팔로우 요청을 처리할 권한이 없습니다."),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4040, "사용자를 찾을 수 없습니다."),
    TARGET_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4041, "대상 사용자를 찾을 수 없습니다."),
    NOT_BLOCKED(HttpStatus.NOT_FOUND, 4042, "차단 상태가 아닙니다."),
    FOLLOW_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND,4042, "팔로우 요청을 찾을 수 없습니다."),

    // 409
    CANNOT_FOLLOW_BLOCKED_USER(HttpStatus.CONFLICT, 4095, "차단한 사용자는 팔로우할 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, 4096, "이미 팔로우한 사용자입니다."),
    ALREADY_REQUESTED(HttpStatus.CONFLICT, 4098, "이미 팔로우 요청을 보낸 사용자입니다."),
    BLOCK_RELATION_EXISTS(HttpStatus.CONFLICT,4097, "차단 관계가 존재하여 요청을 처리할 수 없습니다."),
    FOLLOW_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT,4092, "이미 처리된 팔로우 요청입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, 4099, "이미 해당 상품에 대한 리뷰가 존재합니다."),

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

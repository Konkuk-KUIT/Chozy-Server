package com.kuit.chozy.global.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 400
    SELF_FOLLOW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4003, "자기 자신은 팔로우할 수 없습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, 4001, "요청 값이 올바르지 않습니다."),
    SELF_BLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4004, "자기 자신은 차단할 수 없습니다."),
    SELF_UNBLOCK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4006, "자기 자신에 대한 차단은 해제할 수 없습니다."),
    ALREADY_BLOCKED(HttpStatus.BAD_REQUEST, 4005, "이미 차단한 사용자입니다."),
    SELF_CLOSE_FRIEND_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 4007, "자기 자신은 친한 친구로 설정할 수 없습니다."),
    INVALID_REQUEST_VALUE(HttpStatus.BAD_REQUEST, 4001, "요청 값이 올바르지 않습니다."),

    INVALID_REVIEW_REQUEST(HttpStatus.BAD_REQUEST, 4008, "리뷰 요청 값이 올바르지 않습니다."),
    INVALID_REVIEW_UPDATE_REQUEST(HttpStatus.BAD_REQUEST, 4009, "리뷰 수정 요청 값이 올바르지 않습니다."),
    INVALID_REPOST_REQUEST(HttpStatus.BAD_REQUEST, 4010, "리포스트 요청 값이 올바르지 않습니다."),
    INVALID_QUOTE_REQUEST(HttpStatus.BAD_REQUEST, 4011, "인용 요청 값이 올바르지 않습니다."),

    // 401
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 4010, "아이디 또는 비밀번호가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4012, "인증이 필요합니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 4013, "인증 정보가 유효하지 않습니다. 다시 로그인해 주세요."),

    // 403
    DEACTIVATED_ACCOUNT(HttpStatus.FORBIDDEN, 4030, "비활성화된 계정입니다."),
    FEED_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, 4033, "본인 게시글만 삭제할 수 있습니다."),
    FEED_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, 4035, "본인 게시글만 수정할 수 있습니다."),
    FOLLOW_REQUEST_FORBIDDEN(HttpStatus.FORBIDDEN, 4032, "해당 팔로우 요청을 처리할 권한이 없습니다."),
    REVIEW_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, 4034, "리뷰 작성자만 수정할 수 있습니다."),

    // 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4040, "사용자를 찾을 수 없습니다."),
    FEED_NOT_FOUND(HttpStatus.NOT_FOUND, 4043, "피드를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, 4044, "댓글을 찾을 수 없습니다."),
    TARGET_USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4041, "대상 사용자를 찾을 수 없습니다."),
    NOT_BLOCKED(HttpStatus.NOT_FOUND, 4042, "차단 상태가 아닙니다."),
    FOLLOW_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, 4042, "팔로우 요청을 찾을 수 없습니다."),

    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, 4045, "리뷰를 찾을 수 없습니다."),
    REPOST_NOT_FOUND(HttpStatus.NOT_FOUND, 4046, "리포스트를 찾을 수 없습니다."),

    // 409
    CANNOT_FOLLOW_BLOCKED_USER(HttpStatus.CONFLICT, 4095, "차단한 사용자는 팔로우할 수 없습니다."),
    ALREADY_FOLLOWING(HttpStatus.CONFLICT, 4096, "이미 팔로우한 사용자입니다."),
    ALREADY_REQUESTED(HttpStatus.CONFLICT, 4098, "이미 팔로우 요청을 보낸 사용자입니다."),
    BLOCK_RELATION_EXISTS(HttpStatus.CONFLICT, 4097, "차단 관계가 존재하여 요청을 처리할 수 없습니다."),

    DUPLICATE_LOGIN_ID(HttpStatus.CONFLICT, 4091, "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, 4092, "이미 사용 중인 이메일입니다."),

    FOLLOW_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, 4093, "이미 처리된 팔로우 요청입니다."),
    REVIEW_ALREADY_DELETED(HttpStatus.CONFLICT, 4094, "이미 삭제된 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, 4099, "이미 해당 상품에 대한 리뷰가 존재합니다."),
    REPOST_ALREADY_EXISTS(HttpStatus.CONFLICT, 40910, "이미 리포스트한 게시글입니다."),
    QUOTE_ALREADY_EXISTS(HttpStatus.CONFLICT, 40911, "이미 인용한 게시글입니다."),
    CANNOT_REPOST_WHEN_QUOTED(HttpStatus.CONFLICT, 40912, "인용한 게시글은 리포스트할 수 없습니다."),
    CANNOT_QUOTE_WHEN_REPOSTED(HttpStatus.CONFLICT, 40913, "리포스트한 게시글은 인용할 수 없습니다."),

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

package com.example.taste.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseCode {
	ALREADY_LOGIN(HttpStatus.BAD_REQUEST, "A001", "이미 로그인 된 사용자입니다."),
	UNAUTHENTICATED(HttpStatus.UNAUTHORIZED, "A002", "인증되지 않은 사용자입니다."),
	UNAUTHORIZED(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다."),
	INVALID_ROLE(HttpStatus.UNAUTHORIZED, "A004", "유효하지 않은 권한입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

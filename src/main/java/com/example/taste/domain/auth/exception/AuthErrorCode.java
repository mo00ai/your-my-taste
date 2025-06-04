package com.example.taste.domain.auth.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseCode {
	ALREADY_LOGIN(HttpStatus.BAD_REQUEST, "A001", "이미 로그인 된 사용자입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

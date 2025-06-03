package com.example.taste.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseCode {
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U001", "비밀번호가 일치하지 않습니다."),
	NOT_FOUND_USER(HttpStatus.BAD_REQUEST, "U002", "해당 유저를 찾을 수 없습니다."),
	DEACTIVATED_USER(HttpStatus.BAD_REQUEST, "U003", "해당 유저를 찾을 수 없습니다."),
	CONFLICT_EMAIL(HttpStatus.BAD_REQUEST, "U004", "중복 이메일입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

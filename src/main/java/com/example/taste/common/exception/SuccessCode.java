package com.example.taste.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessCode implements BaseCode {
	OK(HttpStatus.OK, "S001", "요청을 성공했습니다."),
	CREATED(HttpStatus.CREATED, "S002", "요청이 생성되었습니다."),
	LOGOUT_SUCCESS(HttpStatus.OK, "S003", "로그아웃이 성공적으로 처리되었습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

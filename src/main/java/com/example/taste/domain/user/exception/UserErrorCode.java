package com.example.taste.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseCode {
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U001", "비밀번호가 일치하지 않습니다."),
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "유저 정보가 존재하지 않습니다."),
	FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "팔로우 정보가 존재하지 않습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

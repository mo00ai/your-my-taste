package com.example.taste.domain.favor.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum FavorErrorCode implements BaseCode {
	NOT_FOUND_FAVOR(HttpStatus.BAD_REQUEST, "F001", "입맛 취향을 찾을 수 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
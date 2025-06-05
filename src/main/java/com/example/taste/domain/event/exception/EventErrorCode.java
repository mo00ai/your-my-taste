package com.example.taste.domain.event.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements BaseCode {
	NOT_FOUND_EVENT(HttpStatus.BAD_REQUEST, "E001", "이벤트를 찾을 수 없습니다."),
	
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

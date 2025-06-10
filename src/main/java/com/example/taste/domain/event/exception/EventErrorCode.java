package com.example.taste.domain.event.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements BaseCode {
	NOT_FOUND_EVENT(HttpStatus.NOT_FOUND, "E001", "이벤트를 찾을 수 없습니다."),
	ALREADY_APPLIED(HttpStatus.CONFLICT, "E002", "이미 해당 이벤트에 신청된 게시글입니다."),
	UNAUTHORIZED_APPLY(HttpStatus.FORBIDDEN, "E003", "이 게시글에 대한 신청 권한이 없습니다."),
	INVALID_EVENT_PERIOD(HttpStatus.BAD_REQUEST, "E004", "이벤트 기간이 아닙니다."),
	NOT_FOUND_BOARD_EVENT(HttpStatus.NOT_FOUND, "E005", "이벤트 신청 정보를 찾을 수 없습니다."),
	EVENT_REGISTER_BLOCKED(HttpStatus.BAD_REQUEST, "E006", "이벤트 시작일 이후에 작성된 게시글만 이벤트 신청할 수 있습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

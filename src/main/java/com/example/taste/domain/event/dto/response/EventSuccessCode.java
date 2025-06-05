package com.example.taste.domain.event.dto.response;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventSuccessCode implements BaseCode {

	EVENT_CREATED(HttpStatus.CREATED, "E001", "이벤트가 성공적으로 등록되었습니다."),
	EVENT_LIST_FETCHED(HttpStatus.OK, "E002", "이벤트 목록이 성공적으로 조회되었습니다."),
	EVENT_UPDATED(HttpStatus.OK, "E003", "이벤트가 성공적으로 수정되었습니다."),
	EVENT_DELETED(HttpStatus.OK, "E004", "이벤트가 성공적으로 삭제되었습니다."),

	EVENT_APPLIED(HttpStatus.OK, "E005", "이벤트에 성공적으로 신청되었습니다."),
	EVENT_APPLICATION_CANCELED(HttpStatus.OK, "E006", "이벤트 신청이 취소되었습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

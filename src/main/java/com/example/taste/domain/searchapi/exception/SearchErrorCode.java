package com.example.taste.domain.searchapi.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchErrorCode implements BaseCode {
	INCORRECT_QUERY_REQUEST(HttpStatus.BAD_REQUEST, "S001", "잘못된 쿼리요청입니다."),
	INVALID_DISPLAY_VALUE(HttpStatus.BAD_REQUEST, "S002", "부적절한 display 값입니다."),
	INVALID_START_VALUE(HttpStatus.BAD_REQUEST, "S003", "부적절한 start 값입니다."),
	INVALID_SORT_VALUE(HttpStatus.BAD_REQUEST, "S004", "부적절한 sort 값입니다."),
	INVALID_SEARCH_API(HttpStatus.NOT_FOUND, "S005", "존재하지 않는 검색 API입니다."),
	MALFORMED_ENCODING(HttpStatus.BAD_REQUEST, "S006", "잘못된 형식의 인코딩입니다."),

	SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S099", "시스템 에러가 발생했습니다."),

	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

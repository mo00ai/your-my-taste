package com.example.taste.domain.pk.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PkErrorCode implements BaseCode {

	PK_CRITERIA_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "요청하신 PK 포인트 기준을 찾을 수 없습니다."),
	DUPLICATE_PK_TYPE(HttpStatus.BAD_REQUEST, "P002", "중복된 PK 포인트 유형입니다."),
	PK_POINT_OVERFLOW(HttpStatus.BAD_REQUEST, "P003", "포인트 오버플로우가 발생할 수 있습니다."),
	PK_LOG_BULK_INSERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P004", "PK 로그 저장에 실패했습니다."),
	PK_RANKERS_NOT_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "P005", "PK 랭킹에 등록할 유저가 없습니다."),
	PK_TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "P006", "요청하신 PK 기수를 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

package com.example.taste.common.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode implements BaseCode {

	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "유효하지 않은 입력 값입니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 요청 방식입니다."),
	ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "요청한 엔티티를 찾을 수 없습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "내부 서버 오류가 발생했습니다."),
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C005", "유효하지 않은 타입의 값입니다."),

	//AUTH
	INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "A001", "인증되지 않은 사용자입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A002", "접근 권한이 없습니다."),
	INVALID_ROLE(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 권한입니다."),

	//File
	FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일을 읽는 중 서버 오류가 발생했습니다."),
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "파일 업로드 실패했습니다."),

	//Redis
	REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R001", "Redis 작업 처리 중 오류가 발생했습니다."),
	REDIS_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R002", "Redis 직렬화 중 오류가 발생했습니다."),
	REDIS_DESERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R003", "Redis 역직렬화 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

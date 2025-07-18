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
	INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "C006", "요청 가능한 최대 페이지 크기를 초과했습니다."),

	//File
	FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일을 읽는 중 서버 오류가 발생했습니다."),
	FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F002", "파일 업로드 실패했습니다."),

	//Redis
	REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R001", "Redis 작업 처리 중 오류가 발생했습니다."),
	REDIS_SERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R002", "Redis 직렬화 중 오류가 발생했습니다."),
	REDIS_DESERIALIZATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "R003", "Redis 역직렬화 중 오류가 발생했습니다."),
	REDIS_FAIL_GET_LOCK(HttpStatus.INTERNAL_SERVER_ERROR, "R004", "Redis 락 점유 과정에서 오류가 발생했습니다."),

	//Cache
	CACHE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "CA001", "Cache가 존재하지 않습니다."),

	// embedding
	EMBEDDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "임베딩 생성에 실패했습니다: "),
	EMBEDDING_TEXT_NOT_FOUND(HttpStatus.BAD_REQUEST, "E002", "임베딩할 텍스트가 없습니다."),

	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

package com.example.taste.domain.store.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements BaseCode {
	// bucket
	BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "버킷 정보가 존재하지 않습니다."),
	BUCKET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "B002", "접근 권한이 없는 버킷입니다."),
	BUCKET_NAME_OVERFLOW(HttpStatus.BAD_REQUEST, "B003", "버킷 이름에 포함된 숫자가 허용된 범위를 초과했습니다."),

	// store
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "ST001", "맛집 정보가 존재하지 않습니다."),

	// category
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "카테고리 정보가 존재하지 않습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

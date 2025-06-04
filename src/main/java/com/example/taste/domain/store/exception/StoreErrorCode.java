package com.example.taste.domain.store.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StoreErrorCode implements BaseCode {
	// bucket
	BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "버킷 정보가 존재하지 않습니다."),
	BUCKET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "B002", "접근 권한이 없는 버킷입니다."),

	// store
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S002", "맛집 정보가 존재하지 않습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

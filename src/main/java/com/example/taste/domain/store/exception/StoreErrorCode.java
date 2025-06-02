package com.example.taste.domain.store.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseCode {
	// Todo : UserErrorCode 파일로 이동
	USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "U001", "유저 정보가 존재하지 않습니다."),
	FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "팔로우 정보가 존재하지 않습니다."),

	// bucket
	BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "버킷 정보가 존재하지 않습니다."),
	BUCKET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "B002", "접근 권한이 없는 버킷입니다."),

	// store
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S002", "맛집 정보가 존재하지 않습니다.");

	private HttpStatus httpStatus;
	private String code;
	private String message;
}

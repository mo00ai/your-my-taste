package com.example.taste.domain.store.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreErrorCode implements BaseCode {
	// Todo : UserErrorCode 파일로 이동
	USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "401", "유저 정보가 존재하지 않습니다."),
	BUCKET_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "맛집리스트 정보가 존재하지 않습니다."),
	STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "404", "맛집 정보가 존재하지 않습니다.");

	private HttpStatus httpStatus;
	private String code;
	private String message;
}

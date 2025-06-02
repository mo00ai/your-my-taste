package com.example.taste.domain.image.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements BaseCode {

	FAILED_WRITE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "I001", "파일을 저장하는 중 오류가 발생했습니다."),
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "이미지를 찾을 수 없습니다."),
	FAILED_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "I002", "파일을 삭제하는 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}


package com.example.taste.domain.review.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements BaseCode {
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다."),
	NO_IMAGE_REQUESTED(HttpStatus.BAD_REQUEST, "R002", "영수증 이미지가 등록되지 않았습니다."),
	STORE_NAME_NOT_FOUND(HttpStatus.BAD_REQUEST, "R003", "영수증에서 가게 이름을 찾을 수 없습니다."),
	OCR_CALL_FAILED(HttpStatus.BAD_REQUEST, "R004", "OCR 요청에 실패했습니다."),
	REVIEW_USER_MISMATCH(HttpStatus.UNAUTHORIZED, "R005", "본인이 작성한 리뷰가 아닙니다."),
	BAD_OCR_IMAGE(HttpStatus.BAD_REQUEST, "R006", "OCR 요청 이미지가 올바르지 않습니다."),
	BAD_OCR_RESPONSE(HttpStatus.NOT_FOUND, "R007", "OCR 응답이 올바르지 않습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

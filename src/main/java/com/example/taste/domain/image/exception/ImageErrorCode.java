package com.example.taste.domain.image.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements BaseCode {

	//ImageAspect
	INVALID_IMAGE_SIZE(HttpStatus.BAD_REQUEST, "I001", "이미지 파일의 크기는 최대 2MB까지 업로드 가능합니다."),
	INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "I002", "이미지 파일은 .jpg .jpeg .png만 업로드 할 수 있습니다."),
	INVALID_MIME_TYPE(HttpStatus.BAD_REQUEST, "I003", "파일 내용이 이미지가 아닙니다."),
	FIVE_IMAGES_ALLOWED(HttpStatus.BAD_REQUEST, "I004", "맛집 추천 게시글의 이미지 개수는 5개까지 가능합니다."),
	ONLY_ONE_IMAGE_ALLOWED(HttpStatus.BAD_REQUEST, "I005", "이미지 개수는 1개까지 가능합니다."),

	FAILED_WRITE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "I001", "파일을 저장하는 중 오류가 발생했습니다."),
	IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "이미지를 찾을 수 없습니다."),
	FAILED_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "I003", "파일을 삭제하는 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}


package com.example.taste.domain.comment.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements BaseCode {
	COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "C001", "댓글을 찾을 수 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

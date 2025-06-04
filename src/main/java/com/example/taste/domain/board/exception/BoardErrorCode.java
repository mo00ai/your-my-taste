package com.example.taste.domain.board.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardErrorCode implements BaseCode {

	BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "게시글을 찾을 수 없습니다."),

	BOARD_TYPE_NOT_FOUND(HttpStatus.BAD_REQUEST, "BT001", "존재하지 않는 게시글 타입입니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

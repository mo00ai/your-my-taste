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

	HASHTAG_NOT_FOUND(HttpStatus.BAD_REQUEST, "H001", "존재하지 않는 해시태그입니다."),
	INVALID_HASHTAG(HttpStatus.BAD_REQUEST, "H002", "유효하지 않은 해시태그입니다."),
	EMPTY_HASHTAG_LIST(HttpStatus.BAD_REQUEST, "H003", "입력된 해시태그가 없습니다."),

	ALREADY_LIKED(HttpStatus.BAD_REQUEST, "L001", "이미 좋아요을 눌렀습니다."),
	LIKE_NOT_FOUND(HttpStatus.BAD_REQUEST, "L002", "좋아요를 찾을 수 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

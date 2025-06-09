package com.example.taste.domain.board.dto.response;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardSuccessCode implements BaseCode {

	BOARD_CREATED(HttpStatus.CREATED, "B001", "게시글이 성공적으로 생성되었습니다."),
	BOARD_UPDATED(HttpStatus.OK, "B002", "게시글이 성공적으로 수정되었습니다."),
	BOARD_DELETED(HttpStatus.OK, "B003", "게시글이 성공적으로 삭제되었습니다."),
	BOARD_LOADED(HttpStatus.OK, "B004", "게시글 조회가 성공적으로 완료되었습니다."),
	BOARD_LIST_LOADED(HttpStatus.OK, "B005", "게시글 목록 조회가 성공적으로 완료되었습니다."),
	BOARD_LIKED(HttpStatus.OK, "B006", "게시글에 좋아요가 추가됐습니다."),
	BOARD_UNLIKED(HttpStatus.OK, "B007", "게시글 좋아요가 취소됐습니다."),

	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}

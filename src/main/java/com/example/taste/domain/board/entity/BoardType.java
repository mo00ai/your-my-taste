package com.example.taste.domain.board.entity;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BoardType {
	N("N", "일반 게시글"),
	O("O", "오픈런 게시글");

	private final String code;
	private final String displayName;

	BoardType(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	@JsonCreator
	public static BoardType from(String input) {
		for (BoardType type : BoardType.values()) {
			if (type.code.equalsIgnoreCase(input)) {
				return type;
			}
		}
		throw new CustomException(BoardErrorCode.BOARD_TYPE_NOT_FOUND);
	}

	@JsonValue
	public String getCode() {
		return code;
	}

	public boolean matches(String type) {
		return N == from(type);
	}
}

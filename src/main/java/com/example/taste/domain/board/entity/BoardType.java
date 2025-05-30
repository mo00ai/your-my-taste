package com.example.taste.domain.board.entity;

public enum BoardType {
	N("N", "일반 게시글"),
	H("H", "홍대병 게시글");

	private final String code;
	private final String displayName;

	BoardType(String code, String displayName) {
		this.code = code;
		this.displayName = displayName;
	}

	public static BoardType from(String input) {
		for (BoardType type : BoardType.values()) {
			if (type.name().equalsIgnoreCase(input)) {
				return type;
			}
		}
		// TODO 추후 글로벌 예외처리 예정
		throw new IllegalArgumentException("해당하는 Type이 없습니다: " + input);
	}
}

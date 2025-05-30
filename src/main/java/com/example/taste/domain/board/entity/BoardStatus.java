package com.example.taste.domain.board.entity;

import lombok.Getter;

@Getter
public enum BoardStatus {
	// OPEN, CLOSED,  FCFS, TIMEATTACK
	OPEN,        // 오픈
	CLOSED,        // 클로즈
	FCFS,        // 선착순 N명
	TIMEATTACK,    // 제한시간
	;

	public static BoardStatus from(String input) {
		for (BoardStatus status : BoardStatus.values()) {
			if (status.name().equalsIgnoreCase(input)) {
				return status;
			}
		}
		// TODO 추후 글로벌 예외처리 예정
		throw new IllegalArgumentException("해당하는 Status가 없습니다: " + input);
	}

}

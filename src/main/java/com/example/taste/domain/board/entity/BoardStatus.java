package com.example.taste.domain.board.entity;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.exception.BoardErrorCode;

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
		throw new CustomException(BoardErrorCode.BOARD_STATUS_NOT_FOUND);
	}

}

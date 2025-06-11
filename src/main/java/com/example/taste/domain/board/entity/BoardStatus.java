package com.example.taste.domain.board.entity;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.exception.BoardErrorCode;

import lombok.Getter;

@Getter
public enum BoardStatus {
	// OPEN, CLOSED,  FCFS, TIMEATTACK
	OPEN,        // 전체공개
	CLOSED,        // 비공개
	FCFS,        // 선착순 N명에게 공개예정
	TIMEATTACK,    // 특정 기간동안 공개예정
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

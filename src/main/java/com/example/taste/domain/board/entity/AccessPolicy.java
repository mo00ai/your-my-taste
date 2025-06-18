package com.example.taste.domain.board.entity;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.exception.BoardErrorCode;

import lombok.Getter;

@Getter
public enum AccessPolicy {
	OPEN,        // 전체공개
	CLOSED,        // 비공개
	FCFS,        // 선착순 N명에게 공개예정
	TIMEATTACK,    // 특정 기간동안 공개예정
	;

	public static AccessPolicy from(String input) {
		for (AccessPolicy accessPolicy : AccessPolicy.values()) {
			if (accessPolicy.name().equalsIgnoreCase(input)) {
				return accessPolicy;
			}
		}
		throw new CustomException(BoardErrorCode.BOARD_STATUS_NOT_FOUND);
	}

}

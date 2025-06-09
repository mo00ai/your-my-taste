package com.example.taste.domain.match.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum MatchErrorCode implements BaseCode {
	USER_MATCH_COND_NOT_FOUND(HttpStatus.NOT_FOUND, "UMC001", "해당 매칭 조건을 찾을 수 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

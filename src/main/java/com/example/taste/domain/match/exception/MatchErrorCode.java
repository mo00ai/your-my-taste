package com.example.taste.domain.match.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum MatchErrorCode implements BaseCode {
	// 매칭
	ACTIVE_MATCH_EXISTS(HttpStatus.BAD_REQUEST, "M001", "현재 매칭이 진행 중에 있습니다."),

	// 매칭 조건
	USER_MATCH_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "UMC001", "해당 매칭 정보를 찾을 수 없습니다."),
	FORBIDDEN_USER_MATCH_INFO(HttpStatus.FORBIDDEN, "UMC002", "자신의 매칭 정보가 아닙니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

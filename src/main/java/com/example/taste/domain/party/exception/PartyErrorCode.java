package com.example.taste.domain.party.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum PartyErrorCode implements BaseCode {
	PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "파티를 찾을 수 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
package com.example.taste.domain.pk.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PkErrorCode implements BaseCode {

	PK_CRITERIA_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "요청하신 PK 점수 기준을 찾을 수 없습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

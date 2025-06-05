package com.example.taste.domain.party.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

@Getter
@RequiredArgsConstructor
public enum PartyErrorCode implements BaseCode {
	PARTY_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "파티를 찾을 수 없습니다."),
	UNAUTHORIZED_PARTY(HttpStatus.BAD_REQUEST, "P002", "파티장 권한이 없습니다."),
	MAX_CAPACITY_LESS_THAN_CURRENT(
		HttpStatus.BAD_REQUEST, "P003", "파티 최대 인원은 현재 인원 이상이어야 합니다."),
	NOT_RECRUITING_PARTY(HttpStatus.BAD_REQUEST, "P004", "현재 모집 중인 파티가 아닙니다"),

	PARTY_INVITATION_NOT_FOUND(
		HttpStatus.NOT_FOUND, "PI001", "존재하지 않는 파티 초대 정보입니다."),
	ALREADY_EXISTS_PARTY_INVITATION(
		HttpStatus.NOT_FOUND, "PI002", "파티 초대 정보가 이미 존재합니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
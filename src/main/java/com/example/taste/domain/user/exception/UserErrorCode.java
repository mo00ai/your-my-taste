package com.example.taste.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements BaseCode {
	INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U001", "비밀번호가 일치하지 않습니다."),
	NOT_FOUND_USER(HttpStatus.NOT_FOUND, "U002", "해당 유저를 찾을 수 없습니다."),
	DEACTIVATED_USER(HttpStatus.BAD_REQUEST, "U004", "탈퇴한 유저입니다"),
	CONFLICT_EMAIL(HttpStatus.BAD_REQUEST, "U005", "중복 이메일입니다."),
	POSTING_COUNT_OVERFLOW(HttpStatus.CONFLICT, "U006", "포스팅 허용 횟수를 초과했습니다."),
	USER_POINT_RESET_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "U007", "사용자 포인트 초기화에 실패했습니다"),

	FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "F001", "팔로우 정보가 존재하지 않습니다."),
	ALREADY_FOLLOWED(HttpStatus.BAD_REQUEST, "F002", "이미 팔로우된 유저입니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

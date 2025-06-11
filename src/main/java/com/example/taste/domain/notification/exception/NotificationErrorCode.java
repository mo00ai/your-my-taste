package com.example.taste.domain.notification.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseCode {
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "리뷰를 찾을 수 없습니다."),
	BAD_JSON_MAPPING(HttpStatus.BAD_REQUEST, "N002",
		"Beep boop notification went crazy. Contact the dumbass who’s in charge."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

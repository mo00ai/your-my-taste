package com.example.taste.domain.notification.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseCode {
	NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "알림을 찾을 수 없습니다."),
	WRONG_NOTIFICATION_CATEGORY(HttpStatus.NOT_FOUND, "N002", "잘못된 알림 카테고리 입니다."),
	CAN_NOT_CRE_NOTIFICATION(HttpStatus.NOT_FOUND, "N003", "알림 생성에 실패했습니다."),
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}

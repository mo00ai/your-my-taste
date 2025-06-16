package com.example.taste.domain.notification;

public enum NotificationType {
	CREAT("생성"),
	UPDATE("업데이트"),
	DELETE("삭제"),
	ACCEPT("승인"),
	DENY("거부"),
	;

	private final String type;

	NotificationType(String type) {
		this.type = type;
	}

	public String getTypeString() {
		return type;
	}
}

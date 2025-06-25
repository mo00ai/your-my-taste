package com.example.taste.domain.notification.entity.enums;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.exception.NotificationErrorCode;

public enum NotificationCategory {
	INDIVIDUAL("개인"),
	SYSTEM("공지사항"),
	MARKETING("마케팅"),
	SUBSCRIBE("구독"),
	PARTY("파티"),
	CHAT("채팅"),
	STORE("맛집"),
	PK("PK"),
	BOARD("게시글"),
	MATCH("매칭"),
	COMMENT("댓글");

	private final String category;

	NotificationCategory(String category) {
		this.category = category;
	}

	public static NotificationCategory from(String name) {
		if (name == null || name.isBlank()) {
			throw new CustomException(NotificationErrorCode.WRONG_NOTIFICATION_CATEGORY);
		}
		name = name.trim();
		for (NotificationCategory category : values()) {
			if (category.name().equalsIgnoreCase(name)) {
				return category;
			}
		}
		throw new CustomException(NotificationErrorCode.WRONG_NOTIFICATION_CATEGORY);
	}

	public String getCategoryText() {
		return category;
	}
}

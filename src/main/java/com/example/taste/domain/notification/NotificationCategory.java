package com.example.taste.domain.notification;

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
	COMMENT("댓글")
	;

	private final String category;

	NotificationCategory(String category) {
		this.category = category;
	}

	public String getCategoryText() {
		return category;
	}
}

package com.example.taste.domain.notification;

public enum NotificationCategory {
	INDIVIDUAL(null, "개인"),
	SYSTEM(null, "시스템"),
	MARKETING(null, "마케팅"),
	SUBSCRIBE(null, "구독"),
	PARTY("/parties", "파티"),
	CHAT(null, "채팅"),
	STORE(null, "맛집"),
	PK(null, "PK"),
	BOARD(null, "게시글"),
	MATCH(null, "매칭"),
	COMMENT(null, "댓글")
	;

	private final String urlPattern;
	private final String category;

	NotificationCategory(String urlPattern, String category) {
		this.urlPattern = urlPattern;
		this.category = category;
	}

	public String buildUrl(NotificationType type, Long id) {
		if (urlPattern == null) {return null;}
		if (type == NotificationType.DELETE || type == NotificationType.DENY ) {return null;}
		return urlPattern + "/" + id;
	}

	public String getCategoryText() {
		return category;
	}
}

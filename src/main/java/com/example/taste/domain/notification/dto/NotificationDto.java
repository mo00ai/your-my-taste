package com.example.taste.domain.notification.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
	private Long contentId;
	private NotificationCategory category;
	private String content;
	private String redirectUrl;
	private boolean isRead;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	public void readIt() {
		this.isRead = b;
	}

	public NotificationDto(NotificationInfo notificationInfo) {
		this.contentId = notificationInfo.getNotificationContent().getId();
		this.category = notificationInfo.getCategory();
		this.content = notificationInfo.getNotificationContent().getContent();
		this.redirectUrl = notificationInfo.getNotificationContent().getRedirectionUrl();
		this.isRead = notificationInfo.getIsRead();
		this.createdAt = notificationInfo.getCreatedAt();
	}
}

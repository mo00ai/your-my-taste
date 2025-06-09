package com.example.taste.domain.notification.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.notification.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRedis {
	private Long uuid;
	private NotificationCategory category;
	private String content;
	private String redirectUrl;
	private boolean isRead;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;

	public void setRead(boolean b) {
		this.isRead = b;
	}
}

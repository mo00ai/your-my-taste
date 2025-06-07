package com.example.taste.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRedis {
	private String content;
	private String redirectUrl;
	private boolean isRead;
}

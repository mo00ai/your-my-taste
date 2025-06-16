package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.NotificationType;
import com.example.taste.domain.user.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPublishDto {
	private User user;
	private NotificationCategory category;
	private NotificationType type;
	private Long redirectionEntity;
	private String additionalText;
}

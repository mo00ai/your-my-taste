package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.NotificationType;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationPublishDto {
	private User user;
	@NotNull(message = "올바르지 않은 알림 생성 요청입니다.")
	private NotificationCategory category;
	@NotNull(message = "올바르지 않은 알림 생성 요청입니다.")
	private NotificationType type;
	private Long redirectionEntityId;
	private String additionalText;
}

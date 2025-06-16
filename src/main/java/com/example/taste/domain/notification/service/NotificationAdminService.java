package com.example.taste.domain.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.taste.domain.notification.NotificationType;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.dto.AdminNotificationRequestDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.redis.NotificationPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationAdminService {
	private final NotificationPublisher notificationPublisher;

	// 시스템 혹은 마케팅 알림

	public void publishNotification(AdminNotificationRequestDto dto) {
		NotificationPublishDto publishDto = NotificationPublishDto.builder()
			.category(dto.getCategory())
			.redirectionEntity(dto.getRedirectEntity())
			.type(NotificationType.CREAT)
			.build();
		notificationPublisher.publish(publishDto);
	}
}

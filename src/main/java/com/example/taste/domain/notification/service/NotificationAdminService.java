package com.example.taste.domain.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.dto.NotificationRequestDto;
import com.example.taste.domain.notification.redis.NotificationPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationAdminService {
	private final NotificationPublisher notificationPublisher;

	// 시스템 혹은 마케팅 알림

	public void publishNotification(NotificationRequestDto dto) {
		NotificationEventDto eventDto = NotificationEventDto.builder()
			.category(dto.getCategory())
			.content(dto.getContents())
			.redirectUrl(dto.getRedirectUrl())
			.createdAt(LocalDateTime.now())
			.read(false)
			.userId(dto.getTargetUserId())
			.build();
		notificationPublisher.publish(eventDto);
	}
}

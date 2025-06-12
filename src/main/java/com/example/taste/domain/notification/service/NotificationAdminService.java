package com.example.taste.domain.notification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.dto.NotificationRequestDto;
import com.example.taste.domain.notification.redis.NotificationPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationAdminService {
	private final NotificationPublisher notificationPublisher;

	// 시스템 혹은 마케팅 알림
	@Transactional
	public void publishNotification(NotificationRequestDto dto) {
		NotificationEventDto eventDto = new NotificationEventDto(
			dto.getCategory(),
			dto.getContents(),
			dto.getRedirectUrl(),
			LocalDateTime.now()
			, false
			, null);
		notificationPublisher.publish(eventDto);
	}
}

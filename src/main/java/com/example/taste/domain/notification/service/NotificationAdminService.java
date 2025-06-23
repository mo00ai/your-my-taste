package com.example.taste.domain.notification.service;

import org.springframework.stereotype.Service;

import com.example.taste.domain.notification.dto.AdminNotificationRequestDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationCategory;
import com.example.taste.domain.notification.entity.NotificationType;
import com.example.taste.domain.notification.redis.NotificationPublisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationAdminService {
	private final NotificationPublisher notificationPublisher;
	private final MeterRegistry meterRegistry;

	// 시스템 혹은 마케팅 알림
	public void publishNotification(AdminNotificationRequestDto dto) {
		Timer.Sample sample = Timer.start(meterRegistry);
		NotificationPublishDto publishDto = NotificationPublishDto.builder()
			.category(NotificationCategory.from(dto.getCategory()))
			.type(NotificationType.CREATE)
			.redirectionUrl(dto.getRedirectUrl())
			.redirectionEntityId(dto.getRedirectEntityId())
			.additionalText(dto.getContents())
			.build();
		notificationPublisher.publish(publishDto);
		sample.stop(meterRegistry.timer("testing"));
	}
}

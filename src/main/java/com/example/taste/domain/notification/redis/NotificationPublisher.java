package com.example.taste.domain.notification.redis;

import org.springframework.stereotype.Service;

import com.example.taste.domain.notification.dto.NotificationPublishDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
	private final NotificationRedisService notificationRedisService;

	//알림 방행
	public void publish(NotificationPublishDto notificationPublishDto) {
		notificationRedisService.publishNotification(notificationPublishDto);
	}
}

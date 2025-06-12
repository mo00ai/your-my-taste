package com.example.taste.domain.notification.redis;

import org.springframework.stereotype.Service;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.notification.dto.NotificationEventDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
	private final RedisService redisService;

	//알림 방행
	public void publish(NotificationEventDto event) {
		redisService.publishNotification(event);
	}
}

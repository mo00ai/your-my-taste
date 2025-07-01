package com.example.taste.domain.notification.redis.strategy;

import com.example.taste.domain.notification.dto.NotificationPublishDto;

public interface NotificationStrategy {
	void handle(NotificationPublishDto dto);
}

package com.example.taste.domain.notification.notificationEvent;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.redis.NotificationPublisher;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final NotificationPublisher notificationPublisher;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(NotificationPublishDto publishDto) {
		notificationPublisher.publish(publishDto);
	}
}

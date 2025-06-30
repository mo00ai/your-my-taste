package com.example.taste.domain.notification.service;

import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taste.domain.notification.dto.AdminNotificationRequestDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.redis.NotificationPublisher;

@ExtendWith(MockitoExtension.class)
class NotificationAdminServiceUnitTest {

	@Mock
	private NotificationPublisher notificationPublisher;

	@InjectMocks
	private NotificationAdminService notificationAdminService;

	@Test
	void publishNotification() {
		// given
		Long everyId = 1L;
		AdminNotificationRequestDto dto = new AdminNotificationRequestDto(
			"SYSTEM", "test", "testUrl", everyId, everyId
		);
		// when
		notificationAdminService.publishNotification(dto);
		// then
		then(notificationPublisher).should().publish(any(NotificationPublishDto.class));
	}

	@Test
	void publishNotificationToUser() {
		// given
		Long everyId = 1L;
		AdminNotificationRequestDto dto = new AdminNotificationRequestDto(
			"SYSTEM", "test", "testUrl", everyId, everyId
		);
		// when
		notificationAdminService.publishNotificationToUser(dto, everyId);
		// then
		then(notificationPublisher).should().publish(any(NotificationPublishDto.class));
	}
}

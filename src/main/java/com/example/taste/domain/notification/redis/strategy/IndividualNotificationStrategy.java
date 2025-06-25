package com.example.taste.domain.notification.redis.strategy;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.CategorySupport;
import com.example.taste.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IndividualNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final NotificationService notificationService;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.INDIVIDUAL);
	}

	@Override
	public void handle(NotificationPublishDto dto) {
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);
		notificationService.sendIndividual(notificationContent, dataDto);
	}
}

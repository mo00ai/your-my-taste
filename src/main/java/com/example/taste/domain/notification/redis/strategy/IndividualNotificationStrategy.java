package com.example.taste.domain.notification.redis.strategy;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.example.taste.common.util.MemoryTracker;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.CategorySupport;
import com.example.taste.domain.notification.service.NotificationService;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IndividualNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final NotificationService notificationService;
	private final MeterRegistry meterRegistry;
	private final MemoryTracker memoryTracker;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.INDIVIDUAL);
	}

	@Override
	public void handle(NotificationPublishDto dto) {
		Timer.Sample sample = Timer.start(meterRegistry);
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);
		memoryTracker.measureMemoryUsage("individual", () -> {
			notificationService.testIndividual(notificationContent, dataDto);
			//notificationService.sendIndividual(notificationContent, dataDto);
		});
		sample.stop(meterRegistry.timer("individual"));
	}
}

package com.example.taste.domain.notification.redis.strategy;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.taste.common.util.MemoryTracker;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.CategorySupport;
import com.example.taste.domain.notification.service.NotificationService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final MeterRegistry meterRegistry;
	private final MemoryTracker memoryTracker;
	private final NotificationService notificationService;
	private final UserRepository userRepository;
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.SYSTEM);
	}

	@Override
	@SuppressWarnings("checkstyle:RegexpMultiline")
	public void handle(NotificationPublishDto dto) {
		Timer.Sample sample = Timer.start(meterRegistry);
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);

		// 페이징 방식
		// 유저를 1000명 단위로 끊어와 보냄
		int page = 0;
		int size = 1000;
		Page<User> users;
		do {
			users = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
			List<User> currentUsers = users.getContent();
			memoryTracker.measureMemoryUsage("sendBunch", () -> {
				notificationService.sendBunch(notificationContent, dataDto, currentUsers);
			});
			entityManager.clear();
			page++;
		} while (users.hasNext());
		sample.stop(meterRegistry.timer("sendBunch"));

	}
}

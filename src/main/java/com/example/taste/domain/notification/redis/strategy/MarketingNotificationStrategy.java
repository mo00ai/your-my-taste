package com.example.taste.domain.notification.redis.strategy;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.example.taste.common.util.MemoryTracker;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.CategorySupport;
import com.example.taste.domain.notification.service.NotificationService;
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
public class MarketingNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final MeterRegistry meterRegistry;
	private final MemoryTracker memoryTracker;
	private final NotificationService notificationService;
	private final UserRepository userRepository;
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.MARKETING);
	}

	@Override
	@SuppressWarnings("checkstyle:RegexpMultiline")
	public void handle(NotificationPublishDto dto) {
		Timer.Sample sample = Timer.start(meterRegistry);
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);

		// reference by id
		int rPage = 0;
		int rSize = 10000;
		Page<Long> userIds;
		do {
			userIds = userRepository.getAllUserIdPage(PageRequest.of(rPage, rSize));
			List<Long> currentUserId = userIds.getContent();
			memoryTracker.measureMemoryUsage("reference", () -> {
				notificationService.sendBunchUsingReference(notificationContent, dataDto, currentUserId);
			});
			entityManager.clear();
			rPage++;
		} while (userIds.hasNext());

		sample.stop(meterRegistry.timer("reference"));
		/**
		 * * @deprecated ID만 필요할 때는 JPA 프록시 참조를 얻기 위해
		 *   entityManager.getReference(User.class, id) 또는
		 *   userRepository.getReferenceById(id) 사용을 권장합니다.
		 */
	}
}

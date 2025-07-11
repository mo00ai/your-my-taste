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
import com.example.taste.domain.user.repository.FollowRepository;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriverNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final MeterRegistry meterRegistry;
	private final MemoryTracker memoryTracker;
	private final NotificationService notificationService;
	private final FollowRepository followRepository;
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.SUBSCRIBE);
	}

	@Override
	public void handle(NotificationPublishDto dto) {
		Timer.Sample sample = Timer.start(meterRegistry);
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);

		// 이 경우 event 가 가진 user id는 게시글을 작성한 유저임
		// 게시글을 작성한 유저를 팔로우 하는 유저를 찾아야 함
		// 팔로우 하는 모든 유저를 가져옴
		int page = 0;
		int size = 10000;
		Page<Long> userIds;
		do {
			PageRequest pageRequest = PageRequest.of(page, size);
			userIds = followRepository.findAllIdByFollowing(dto.getUserId(), pageRequest);
			List<Long> currentUserId = userIds.getContent();
			memoryTracker.measureMemoryUsage("reference", () -> {
				notificationService.sendBunchUsingReference(notificationContent, dataDto, currentUserId);
			});
			entityManager.clear();
			page++;
		} while (userIds.hasNext());
		sample.stop(meterRegistry.timer("subscribeNotifications"));
	}
}

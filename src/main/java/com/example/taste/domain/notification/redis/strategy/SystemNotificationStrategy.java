package com.example.taste.domain.notification.redis.strategy;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.redis.CategorySupport;
import com.example.taste.domain.notification.service.NotificationService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemNotificationStrategy implements NotificationStrategy, CategorySupport {

	private final NotificationService notificationService;
	private final UserRepository userRepository;

	@Override
	public Set<NotificationCategory> getSupportedCategories() {
		return Set.of(NotificationCategory.SYSTEM);
	}

	@Override
	@SuppressWarnings("checkstyle:RegexpMultiline")
	public void handle(NotificationPublishDto dto) {
		NotificationDataDto dataDto = notificationService.makeDataDto(dto);
		NotificationContent notificationContent = notificationService.saveContent(dataDto);
		notificationService.sendIndividual(notificationContent, dataDto);

		// 페이징 방식
		long startLogging = System.currentTimeMillis();
		// 유저를 1000명 단위로 끊어와 보냄
		int page = 0;
		int size = 1000;
		Page<User> users;
		do {
			users = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
			notificationService.sendBunch(notificationContent, dataDto, users.getContent());
			page++;

		} while (users.hasNext());
		long endLogging = System.currentTimeMillis();
		log.info("paging 타임 체크: {} ms", (endLogging - startLogging));

		/*
		// reference by id
		startLogging = System.currentTimeMillis();
		List<Long> userIds = userRepository.findAllUserId();
		notificationService.sendBunchUsingReference(event, userIds);
		endLogging = System.currentTimeMillis();
		log.info("reference by id 타임 체크", (endLogging - startLogging));
		 */

		/**
		 * * @deprecated ID만 필요할 때는 JPA 프록시 참조를 얻기 위해
		 *   entityManager.getReference(User.class, id) 또는
		 *   userRepository.getReferenceById(id) 사용을 권장합니다.
		 */

		// TODO 두 방식 걸리는 시간 비교할 것.
	}
}

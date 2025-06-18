package com.example.taste.domain.notification.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.repository.NotificationInfoRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationInfoRepository infoRepository;
	private final RedisService redisService;
	private final EntityFetcher entityFetcher;

	// 개별 알림
	@Transactional // 둘 중 하나라도 저장 실패시 전부 롤백 하도록
	public void sendIndividual(NotificationContent content, NotificationDataDto dataDto) {
		User user = entityFetcher.getUserOrThrow(dataDto.getUserId());
		redisService.storeNotification(dataDto.getUserId(), content.getId(), dataDto, Duration.ofDays(7));
		infoRepository.save(NotificationInfo.builder()
			.category(dataDto.getCategory())
			.notificationContent(content)
			.user(user)
			.build());
	}

	// 단체알림(마케팅, 시스템)
	@Transactional
	public void sendBunch(NotificationContent content, NotificationDataDto dataDto, List<User> allUser) {
		List<NotificationInfo> notificationInfos = new ArrayList<>();
		for (User user : allUser) {
			redisService.storeNotification(user.getId(), content.getId(), dataDto, Duration.ofDays(7));
			notificationInfos.add(NotificationInfo.builder()
				.category(dataDto.getCategory())
				.notificationContent(content)
				.user(user)
				.build());
		}
		infoRepository.saveAll(notificationInfos);
	}

	/*
	// 단체알림 reference by id (다른 방식으로 구현)
	@Transactional
	public void sendBunchUsingReference(NotificationContent content, NotificationDataDto event, List<Long> allUserId) {
		List<NotificationInfo> notificationInfos = new ArrayList<>();
		for (Long id : allUserId) {
			notificationInfos.add(NotificationInfo.builder()
				.category(event.getCategory())
				.notificationContent(content)
				.user(new User(id))
				.isRead(false)
				.build());
		}
		infoRepository.saveAll(notificationInfos);
	}
	 */
}

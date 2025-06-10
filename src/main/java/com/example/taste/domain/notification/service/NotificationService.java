package com.example.taste.domain.notification.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.notification.dto.NotificationEventDto;
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

	public void sendIndividual(NotificationContent content, NotificationEventDto event, User user) {

		redisService.storeNotification(user.getId(), content.getId(), event, Duration.ofDays(7), false);
		infoRepository.save(NotificationInfo.builder()
			.category(event.getCategory())
			.notificationContent(content)
			.user(user)
			.isRead(false)
			.build());
	}

	@Transactional
	public void sendBunch(NotificationContent content, NotificationEventDto event, List<User> allUser) {
		List<NotificationInfo> notificationInfos = new ArrayList<>();
		for (User user : allUser) {
			redisService.storeNotification(user.getId(), content.getId(), event, Duration.ofDays(7), false);
			notificationInfos.add(NotificationInfo.builder()
				.category(event.getCategory())
				.notificationContent(content)
				.user(user)
				.isRead(false)
				.build());
		}
		infoRepository.saveAll(notificationInfos);
	}

	@Transactional
	public void sendBunchUsingReference(NotificationContent content, NotificationEventDto event, List<Long> allUserId) {
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

}

package com.example.taste.domain.notification.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.domain.notification.dto.NotificationEvent;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.repository.NotificationContentRepository;
import com.example.taste.domain.notification.repository.NotificationInfoRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationContentRepository contentRepository;
	private final NotificationInfoRepository infoRepository;

	public void sendIndividual(NotificationEvent event, User user) {
		NotificationContent content = saveContent(event);

		infoRepository.save(NotificationInfo.builder()
			.category(event.getCategory())
			.notificationContent(content)
			.user(user)
			.isRead(false)
			.build());
	}

	@Transactional
	public void sendBunch(NotificationEvent event, List<User> allUser) {
		NotificationContent content = saveContent(event);
		List<NotificationInfo> notificationInfos = new ArrayList<>();
		for (User user : allUser) {
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
	public void sendBunchUsingReference(NotificationEvent event, List<Long> allUserId) {
		NotificationContent content = saveContent(event);
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

	public NotificationContent saveContent(NotificationEvent event) {
		return contentRepository.save(NotificationContent.builder()
			.content(event.getContent())
			.redirectionUrl(event.getRedirectUrl())
			.build());
	}
}

package com.example.taste.domain.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taste.domain.notification.dto.NotificationEvent;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.repository.NotificationContentRepository;
import com.example.taste.domain.notification.repository.NotificationInfoRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationContentRepository contentRepository;
	private final NotificationInfoRepository infoRepository;
	private final UserRepository userRepository;

	public void sendIndividual(NotificationEvent event, User user) {
		NotificationContent content = saveContent(event);

		infoRepository.save(NotificationInfo.builder()
			.category(event.getCategory())
			.notificationContent(content)
			.user(user)
			.isRead(false)
			.build());
	}

	public void sendBunch(NotificationEvent event, List<User> allUser) {
		NotificationContent content = saveContent(event);
		for (User user : allUser) {
			infoRepository.save(NotificationInfo.builder()
				.category(event.getCategory())
				.notificationContent(content)
				.user(user)
				.isRead(false)
				.build());
		}
	}

	public NotificationContent saveContent(NotificationEvent event) {
		return contentRepository.save(NotificationContent.builder()
			.content(event.getContent())
			.redirectionUrl(event.getRedirectUrl())
			.build());
	}
}

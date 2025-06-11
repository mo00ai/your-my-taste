package com.example.taste.domain.notification.redis;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.repository.NotificationContentRepository;
import com.example.taste.domain.notification.service.NotificationService;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {
	private final NotificationService notificationService;
	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final NotificationContentRepository contentRepository;
	private final GenericJackson2JsonRedisSerializer serializer;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			System.out.println(json);
			//NotificationEventDto event = objectMapper.readValue(json, NotificationEventDto.class);
			NotificationEventDto event = serializer.deserialize(message.getBody(), NotificationEventDto.class);
			switch (event.getCategory()) {
				case INDIVIDUAL -> {
					sendIndividual(event);
				}
				case SYSTEM, MARKETING -> {
					sendSystem(event);
				}
				case SUBSCRIBERS -> {
					sendSubscriber(event);
				}
			}
		} catch (Exception e) {
			log.error("Notification 오류", e);
		}
	}

	private void sendIndividual(NotificationEventDto event) {
		NotificationContent content = saveContent(event);
		User user = userRepository.findById(event.getUserId())
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		notificationService.sendIndividual(content, event, user);
	}

	private void sendSystem(NotificationEventDto event) {
		NotificationContent content = saveContent(event);
		// 페이징 방식
		long startLogging = System.currentTimeMillis();
		int page = 0;
		int size = 100;
		Page<User> users;
		do {
			users = userRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
			notificationService.sendBunch(content, event, users.getContent());
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
	}

	private void sendSubscriber(NotificationEventDto event) {
		NotificationContent content = saveContent(event);
		List<Follow> followList = followRepository.findAllByFollowing(event.getUserId());
		List<User> followers = new ArrayList<>();
		for (Follow follow : followList) {
			followers.add(follow.getFollower());
		}
		notificationService.sendBunch(content, event, followers);
	}

	public NotificationContent saveContent(NotificationEventDto event) {
		return contentRepository.save(NotificationContent.builder()
			.content(event.getContent())
			.redirectionUrl(event.getRedirectUrl())
			.build());
	}
}

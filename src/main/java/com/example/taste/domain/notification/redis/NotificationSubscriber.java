package com.example.taste.domain.notification.redis;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.dto.NotificationEvent;
import com.example.taste.domain.notification.service.NotificationService;
import com.example.taste.domain.user.entity.Follow;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.FollowRepository;
import com.example.taste.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {
	private final ObjectMapper objectMapper;
	private final NotificationService notificationService;
	private final UserRepository userRepository;
	private final FollowRepository followRepository;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		try {
			String json = new String(message.getBody(), StandardCharsets.UTF_8);
			NotificationEvent event = objectMapper.readValue(json, NotificationEvent.class);

			switch (event.getNotificationType()) {
				case INDIVIDUAL -> {
					sendIndividual(event);
				}
				case BROADCAST_ALL -> {
					sendAll(event);
				}
				case BROADCAST_SUBSCRIBERS -> {
					sendSubscriber(event);
				}
			}
		} catch (Exception e) {
			log.error("Notification 오류", e);
		}
	}

	private void sendIndividual(NotificationEvent event) {
		User user = userRepository.findById(event.getUserId())
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
		notificationService.sendIndividual(event, user);
	}

	private void sendAll(NotificationEvent event) {
		List<User> allUser = userRepository.findAll();
		notificationService.sendBunch(event, allUser);
	}

	private void sendSubscriber(NotificationEvent event) {
		List<Follow> followList = followRepository.findAllByFollowing(event.getUserId());
		List<User> followers = new ArrayList<>();
		for (Follow follow : followList) {
			followers.add(follow.getFollower());
		}
		notificationService.sendBunch(event, followers);
	}
}

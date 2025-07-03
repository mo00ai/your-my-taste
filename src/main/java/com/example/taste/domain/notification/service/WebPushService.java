package com.example.taste.domain.notification.service;

import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.PushSubscribeRequestDto;
import com.example.taste.domain.notification.entity.WebPushSubscription;
import com.example.taste.domain.notification.exception.NotificationErrorCode;
import com.example.taste.domain.notification.repository.webPush.WebPushRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPushService {
	private final UserRepository userRepository;
	private final WebPushRepository webPushRepository;
	private final FirebaseMessaging firebaseMessaging;

	@Transactional
	public void saveSubscription(Long userId, PushSubscribeRequestDto dto) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		WebPushSubscription subscription = webPushRepository.findByFcmToken(dto.getFcmToken()).orElse(null);

		if (subscription != null && subscription.getUser().isSameUser(user.getId())) {
			// If the token already exists for the same user, do nothing.
			return;
		} else if (subscription != null) {
			// If the token exists but for a different user, delete the old subscription.
			webPushRepository.delete(subscription);
		}

		subscription = WebPushSubscription.builder()
			.fcmToken(dto.getFcmToken())
			.user(user)
			.build();
		webPushRepository.save(subscription);
	}

	public void deleteSubscription(Long userId, String fcmToken) {
		WebPushSubscription subscription =
			webPushRepository.getWebPushSubscriptionByUserIdAndFcmToken(userId, fcmToken);
		if (subscription != null) {
			webPushRepository.delete(subscription);
		}
	}

	public void send(WebPushSubscription subscription, NotificationDataDto dataDto, Long contentId) {
		try {
			// Create a data payload
			Map<String, String> data = new HashMap<>();
			data.put("category", dataDto.getCategory().name());
			data.put("content", dataDto.getContents());
			data.put("contentId", String.valueOf(contentId));
			data.put("createdAt", dataDto.getCreatedAt().toString());
			data.put("redirectUrl", dataDto.getRedirectionUrl());

			// Create a notification payload (optional, for display notifications)
			Notification notification = Notification.builder()
				.setTitle("New Notification") // You might want to customize this
				.setBody(dataDto.getContents())
				.setImage("/firebase-image.png") // Add this line for the image
				.build();

			// Build the message
			Message message = Message.builder()
				.setToken(subscription.getFcmToken())
				.setNotification(notification) // Optional: for display notifications
				.putAllData(data) // For data messages
				.build();

			// Send the message
			String response = firebaseMessaging.send(message);
			log.info("Successfully sent message: " + response);

		} catch (FirebaseMessagingException e) {
			log.error("Firebase Cloud Messaging send error", e);
			throw new CustomException(NotificationErrorCode.CAN_NOT_CREATE_NOTIFICATION, e.getMessage());
		} catch (Exception e) {
			log.error("WebPush send error", e);
			throw new CustomException(NotificationErrorCode.CAN_NOT_CREATE_NOTIFICATION, e.getMessage());
		}
	}
}

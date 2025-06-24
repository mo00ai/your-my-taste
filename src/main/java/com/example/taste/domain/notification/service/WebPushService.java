package com.example.taste.domain.notification.service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.PushSubscribeRequestDto;
import com.example.taste.domain.notification.dto.WebPushPayloadDto;
import com.example.taste.domain.notification.entity.WebPushSubscription;
import com.example.taste.domain.notification.exception.NotificationErrorCode;
import com.example.taste.domain.notification.repository.webPush.WebPushRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPushService {

	private final WebPushRepository webPushRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Value("${vapid.public}")
	private String vapidPublic;
	@Value("${vapid.private}")
	private String vapidPrivate;

	@Transactional
	public void saveSubscription(User user, PushSubscribeRequestDto dto) {
		WebPushSubscription information = webPushRepository.findByEndpoint(dto.getEndPoint()).orElse(null);

		if (information != null && information.getUser().isSameUser(user.getId())) {
			information.setP256dhKey(dto.getKeys().getP256dh());
			information.setAuthKey(dto.getKeys().getAuth());
			return;
		} else if (information != null) {
			webPushRepository.delete(information);
		}

		information = WebPushSubscription.builder()
			.authKey(dto.getKeys().getAuth())
			.endpoint(dto.getEndPoint())
			.p256dhKey(dto.getKeys().getP256dh())
			.user(user)
			.build();
		webPushRepository.save(information);
	}

	public void deleteSubscription(Long userId, String endpoint) {
		WebPushSubscription subscription =
			webPushRepository.getWebPushSubscriptionByUserIdAndEndpoint(userId, endpoint);
		if (subscription != null) {
			webPushRepository.delete(subscription);
		}
	}

	public void send(WebPushSubscription subscription, NotificationDataDto dataDto, Long contentId) {
		try {
			PushService pushService = new PushService()
				.setPublicKey(vapidPublic)
				.setPrivateKey(vapidPrivate)
				.setSubject("some-admin-email@example.com");

			// 페이로드 JSON 직렬화
			WebPushPayloadDto dto = WebPushPayloadDto.builder()
				.category(dataDto.getCategory())
				.content(dataDto.getContents())
				.contentId(contentId)
				.createdAt(dataDto.getCreatedAt())
				.redirectUrl(dataDto.getRedirectionUrl())
				.build();
			String payload = objectMapper.writeValueAsString(dto);
			byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

			// Notification 생성 (구독 정보에서 endpoint, p256dh, auth 사용)
			Notification notification = new Notification(
				subscription.getEndpoint(),
				subscription.getP256dhKey(),
				subscription.getAuthKey(),
				payloadBytes
			);
			log.info(notification.toString());

			pushService.send(notification);
		} catch (Exception e) {
			log.error("WebPush send error", e);  // 로깅 프레임워크 사용 시
			throw new CustomException(NotificationErrorCode.CAN_NOT_CREATE_NOTIFICATION, e.toString());
		}
	}
}

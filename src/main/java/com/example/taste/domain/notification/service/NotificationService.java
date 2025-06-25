package com.example.taste.domain.notification.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.entity.WebPushSubscription;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.entity.enums.NotificationType;
import com.example.taste.domain.notification.redis.NotificationRedisService;
import com.example.taste.domain.notification.repository.notification.NotificationContentRepository;
import com.example.taste.domain.notification.repository.notification.NotificationInfoRepository;
import com.example.taste.domain.notification.repository.webPush.WebPushRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationInfoRepository infoRepository;
	private final NotificationRedisService notificationRedisService;
	private final EntityFetcher entityFetcher;
	private final WebPushService webPushService;
	private final WebPushRepository webPushRepository;
	private final UserRepository userRepository;
	private final NotificationContentRepository notificationContentRepository;

	// 개별 알림
	@Transactional // 둘 중 하나라도 저장 실패시 전부 롤백 하도록
	public void sendIndividual(NotificationContent content, NotificationDataDto dataDto) {
		User user = entityFetcher.getUserOrThrow(dataDto.getUserId());
		List<WebPushSubscription> webPushSubscriptions = webPushRepository.findByUser(user);
		for (WebPushSubscription subscription : webPushSubscriptions) {
			try {
				webPushService.send(subscription, dataDto, content.getId());
			} catch (Exception e) {
				log.error("Failed to send web push to subscription: {}", subscription.getEndpoint(), e);
			}
		}
		notificationRedisService.storeNotification(dataDto.getUserId(), content.getId(), dataDto, Duration.ofDays(7));
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
			List<WebPushSubscription> webPushSubscriptions = webPushRepository.findByUser(user);
			for (WebPushSubscription subscription : webPushSubscriptions) {
				try {
					webPushService.send(subscription, dataDto, content.getId());
				} catch (Exception e) {
					log.error("Failed to send web push to subscription: {}", subscription.getEndpoint(), e);
				}

			}
			notificationRedisService.storeNotification(user.getId(), content.getId(), dataDto, Duration.ofDays(7));
			notificationInfos.add(NotificationInfo.builder()
				.category(dataDto.getCategory())
				.notificationContent(content)
				.user(user)
				.build());
		}
		// 여기서 삭제 메서드 호출
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

	public NotificationDataDto makeDataDto(NotificationPublishDto publishDto) {
		String contents = makeContent(publishDto.getUserId(), publishDto.getCategory(), publishDto.getType(),
			publishDto.getAdditionalText());
		NotificationDataDto dataDto = NotificationDataDto.builder()
			.userId(publishDto.getUserId())
			.category(publishDto.getCategory())
			.contents(contents)
			.createdAt(LocalDateTime.now())
			.build();
		dataDto.buildUrl(publishDto.getRedirectionUrl(), publishDto.getRedirectionEntityId());
		return dataDto;
	}

	private String makeContent(Long userId, NotificationCategory category, NotificationType type,
		String additionalText) {
		if (category.equals(NotificationCategory.SYSTEM) ||
			category.equals(NotificationCategory.MARKETING) ||
			category.equals(NotificationCategory.INDIVIDUAL)) {
			return additionalText;
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND_USER));
		return user.getNickname() + " 이/가"
			+ category.getCategoryText() + " 을/를"
			+ type.getTypeString() + " 했습니다.\n"
			+ additionalText;
	}

	public NotificationContent saveContent(NotificationDataDto dataDto) {
		NotificationContent notificationContent = notificationContentRepository.save(NotificationContent.builder()
			.content(dataDto.getContents())
			.redirectionUrl(dataDto.getRedirectionUrl())
			.build());
		return notificationContent;
	}
}

package com.example.taste.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationRedis;
import com.example.taste.domain.notification.exception.NotificationErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationUserService {
	private final RedisService redisService;

	public GetNotificationCountResponseDto getNotificationCount(CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		String system = "notification:user:" + userId + ":" + NotificationCategory.SYSTEM + ":unreadCount";
		String marketing = "notification:user:" + userId + ":" + NotificationCategory.MARKETING + ":unreadCount";
		String subscribers = "notification:user:" + userId + ":" + NotificationCategory.SUBSCRIBERS + ":unreadCount";
		// TODO 유저가 원하지 않는 카테고리 알림을 여기서 삭제.
		Long count = 0L;
		count += getCount(system);
		count += getCount(marketing);
		count += getCount(subscribers);

		return new GetNotificationCountResponseDto(count);
	}

	private Long getCount(String key) {
		Object value = redisService.getKeyValue(key);
		return (value != null) ? (Long)value : 0L;
	}

	public List<NotificationRedis> getNotificationList(CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		String pattern = "notification:info:user:" + userId;
		Set<String> keys = getKeys(pattern);
		List<NotificationRedis> notifications = new ArrayList<>();
		for (String key : keys) {
			notifications.add(getNotificationRedisOrThrow(key));
		}
		return notifications;
	}

	public void markNotificationAsRead(CustomUserDetails userDetails, Long uuid) {
		Long userId = userDetails.getId();
		String pattern = "notification:info:user:" + userId + ":*:id:" + uuid;
		Set<String> keys = getKeys(pattern);
		String key = keys.iterator().next();
		NotificationRedis notification = getNotificationRedisOrThrow(key);

		if (!notification.isRead()) {
			String countKey = "notification:count:user:" + userId + ":" + notification.getCategory().name();
			redisService.decreaseCount(countKey, 1L);
		}
		notification.setRead(true);
		redisService.updateNotification(notification, key);

	}

	public Set<String> getKeys(String pattern) {
		Set<String> keys = redisService.getKeys(pattern);
		if (keys.isEmpty()) {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}
		return keys;
	}

	public NotificationRedis getNotificationRedisOrThrow(String key) {
		Object obj = redisService.getKeyValue(key);
		if (obj instanceof NotificationRedis notification) {
			return notification;
		}
		throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
	}
}

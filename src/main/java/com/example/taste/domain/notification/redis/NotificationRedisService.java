package com.example.taste.domain.notification.redis;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.example.taste.common.constant.RedisChannel;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationRedisService {

	private final RedisService redisService;

	//Notification store
	public void storeNotification(Long userId, Long contentId, NotificationDataDto dataDto,
		Duration duration) {
		String key = "notification:user:" + userId + ":id:" + contentId + ":" + dataDto.getCategory().name();
		redisService.setKeyValue(key, dataDto, duration);
		// 해당 카테고리에 대해 count 증가
		String countKey = "notification:count:user:" + userId + ":" + dataDto.getCategory().name();
		redisService.increaseCount(countKey);
		String listKey = "notification:list:user:" + userId;
		String lockKey = "lock:" + listKey;
		boolean locked = redisService.setIfAbsent(lockKey, key, Duration.ofSeconds(1));
		if (locked) {
			try {
				redisService.listLeftPush(listKey, key);
				deleteOldNotifications(listKey);
			} finally {
				Object lockValue = redisService.getKeyValue(lockKey);
				if (lockValue.equals(key)) {
					redisService.delete(lockKey);
				}
			}
		} else {
			log.warn("락 획득 실패 {}", lockKey);
		}

	}

	public void deleteOldNotifications(String listKey) {
		Long size = redisService.getListSize(listKey);
		if (size != null && size > 100) {
			long overflow = size - 100;
			for (int i = 0; i < overflow; i++) {
				// redis 에서만 삭제하기 때문에 count 는 유지
				String oldestKey = redisService.getLast(listKey, String.class);
				if (oldestKey != null) {
					redisService.delete(oldestKey);
				}
				redisService.deleteFromList(listKey, oldestKey, String.class);
			}
		}
	}

	public void deleteNotificationOfCategorys(Long userId, List<NotificationCategory> categories) {
		Set<String> categoryStrings = new HashSet<>();
		for (NotificationCategory category : categories) {
			categoryStrings.add(category.name());
			redisService.delete("notification:count:user:" + userId + ":" + category.name());
		}
		String pattern = "notification:user:" + userId + ":id*";

		Set<String> keys = redisService.getKeys(pattern);
		for (String key : keys) {
			String[] split = key.split(":");
			String category = split[split.length - 1];

			if (categoryStrings.contains(category)) {
				redisService.delete(key);
			}
		}
	}

	/*
					NotificationDataDto oldDataDto = (NotificationDataDto)redisService.getKeyValue(oldestKey);
				if (!oldDataDto.isRead()) {
					redisService.decreaseCount(oldestKey);
				}
	 */

	//Notification publish
	public void publishNotification(NotificationPublishDto publishDto) {
		redisService.convertAndSend(RedisChannel.NOTIFICATION_CHANNEL, publishDto);
	}

	public void updateNotification(NotificationDataDto eventDto, String key) {
		Duration duration = redisService.getExpire(key, TimeUnit.SECONDS);
		redisService.setKeyValue(key, eventDto, duration);
	}
}

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
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationRedisService {

	private final RedisService redisService;

	//Notification store
	public Long storeNotification(Long userId, Long contentId, NotificationDataDto dataDto,
		Duration duration) {
		String key = "notification:user:" + userId + ":id:" + contentId + ":" + dataDto.getCategory().name();
		redisService.setKeyValue(key, dataDto, duration);
		// 해당 카테고리에 대해 count 증가
		String countKey = "notification:count:user:" + userId + ":" + dataDto.getCategory().name();
		redisService.increaseCount(countKey);

		String listKey = "notification:list:user:" + userId;
		redisService.listLeftPush(listKey, key);
		return redisService.getListSize(listKey);
	}

	public void storeAndTrimNotification(Long userId, Long contentId, NotificationDataDto dataDto) {
		if (userId == 0 || contentId == null || dataDto == null || dataDto.getCategory() == null) {
			throw new IllegalArgumentException("Invalid params");
		}

		String key = "notification:user:" + userId + ":id:" + contentId + ":" + dataDto.getCategory().name();
		String listKey = "notification:list:user:" + userId;
		String countKey = "notification:count:user:" + userId + ":" + dataDto.getCategory().name();

		// Lua로 redis 일괄 작업 (삭제 포함)
		try {
			redisService.execute(countKey, listKey, key, dataDto);
		} catch (JsonProcessingException e) {
			log.warn("redis 저장 실패");
		}

	}

	public void deleteNotificationOfCategories(Long userId, List<NotificationCategory> categories) {
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

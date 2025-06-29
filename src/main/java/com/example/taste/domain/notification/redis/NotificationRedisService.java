package com.example.taste.domain.notification.redis;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
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

	public void testStoreAndTrimNotification(Long userId, Long contentId, NotificationDataDto dataDto) {
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

	public void deleteOldNotifications(Long userId) {

		redisService.getKeyValue("dummyKey");

		String lockToken = UUID.randomUUID().toString();
		String listKey = "notification:list:user:" + userId;
		String lockKey = "lock:" + listKey;
		int retryCount = 0;
		while (retryCount < 3) {
			boolean locked = redisService.transactionalSetIfAbsent(lockKey, lockToken, Duration.ofSeconds(3));
			if (locked) {
				try {
					// 락 걸었으면
					// finally 실행 하고 return 함
					Long size = redisService.getListSize(listKey);
					if (size != null && size > 100) {
						List<Object> toDelete = redisService.getListRange(listKey, 100, -1); // 101번째부터 끝까지
						for (Object key : toDelete) {
							if (key instanceof String) {
								log.info("deleting {}", key);
								redisService.delete((String)key); // 알림 실제 내용 삭제
							}
						}
						// 리스트를 100개로 자름
						redisService.listTrim(listKey, 0, 99);
					}
					return;
				} finally {
					Object lockValue = redisService.getKeyValue(lockKey);
					if (lockValue != null && lockValue.equals(lockToken)) {
						redisService.delete(lockKey);
					}
				}
			} else {
				// 락 못 걸었으면(다른데서 락이 걸려있으면)
				retryCount++;
				if (retryCount >= 3) {
					log.error("락 획득 실패 {}", lockKey);
				}
				try {
					// waiting jitter
					// thread local random 을 쓰면 각 스레드 별로 난수 생성기를 사용(다른 스레드 난수 생성을 기다릴 필요 없음)
					Thread.sleep(ThreadLocalRandom.current().nextInt(retryCount * 100) + 100);
				} catch (InterruptedException e) {
					throw new RuntimeException("Interrupted while waiting", e);
				}
			}
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

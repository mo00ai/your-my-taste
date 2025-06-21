package com.example.taste.domain.notification.redis;

import java.time.Duration;

import org.springframework.stereotype.Service;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.notification.dto.NotificationDataDto;

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
		// 모든 카테고리에 대해 count 증가
		String countKey = "notification:count:user:" + userId + ":" + dataDto.getCategory().name();
		redisService.increaseCount(countKey);
		String listKey = "notification:list:user:" + userId;
		redisService.listLeftPush(listKey, key);
		Long size = redisService.getListSize(listKey);
		log.info("size is " + size);
		if (size != null && size > 100) {
			long overflow = size - 100;
			log.info("trimming" + overflow);
			for (int i = 0; i < overflow; i++) {
				String oldestKey = redisService.getLast(listKey, String.class);
				log.info("Popping from listKey={} -> value={}", listKey, oldestKey);
				if (oldestKey != null) {
					log.info("delete called");
					redisService.delete(oldestKey);
				}
				redisService.deleteFromList(listKey, oldestKey, String.class);
			}
		}
	}
}

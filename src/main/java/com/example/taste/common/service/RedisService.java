package com.example.taste.common.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.taste.domain.notification.dto.NotificationEvent;
import com.example.taste.domain.notification.dto.NotificationRedis;
import com.example.taste.domain.notification.redis.RedisChannel;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisTemplate<String, String> stringRedisTemplate;
	private final ObjectMapper objectMapper;

	public RedisService(RedisTemplate<String, Object> redisTemplate,
		RedisTemplate<String, String> stringRedisTemplate,
		ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
	}

	public RedisConnection getRedisConnection() {
		return Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
	}

	/**
	 * 사용자가 필요한 set 메서드가 더 있다면 직접 만들어서 사용하세요
	 */

	// 키가 없으면 값을 설정하고 true 반환, 키가 이미 존재하면 아무 작업도 하지 않고 false 반환
	public boolean setIfAbsent(String key, Object value, Duration validityTime) {
		return Boolean.TRUE.equals(
			redisTemplate.opsForValue().setIfAbsent(key, value, validityTime));
	}

	// key, String
	public void setKeyValue(String key, Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	// key, String, Duration
	public void setKeyValue(String key, String value, Duration validityTime) {
		redisTemplate.opsForValue().set(key, value, validityTime);
	}

	// key, Object, Duration
	public void setKeyValue(String key, Object value, Duration validityTime) {
		redisTemplate.opsForValue().set(key, value, validityTime);
	}

	// key, Long, Duration
	public void setKeyValue(String key, Long value, Duration validityTime) {
		redisTemplate.opsForValue().set(key, value, validityTime);
	}

	// key, List<String>, Duration
	public void setKeyValues(String key, List<String> values, Duration validityTime) {
		redisTemplate.opsForValue().set(key, values, validityTime);
	}

	public void setOpsForList(String key, Object value, Duration duration) {
		boolean keyExists = Boolean.TRUE.equals(redisTemplate.hasKey(key));
		redisTemplate.opsForList().rightPush(key, value);
		if (!keyExists) {
			redisTemplate.expire(key, duration);
		}
	}

	//Notification publish
	public void publishNotification(NotificationEvent event) {
		redisTemplate.convertAndSend(RedisChannel.NOTIFICATION_CHANNEL, event);
	}

	//Notification store
	@Builder
	public void storeNotification(Long userId, Long uuid, NotificationEvent event,
		Duration duration, Boolean isRead) {
		NotificationRedis dto = new NotificationRedis(uuid, event.getCategory(), event.getContent(),
			event.getRedirectUrl(),
			isRead,
			LocalDateTime.now());
		String key = "notification:info:user:" + userId + ":id:" + uuid + ":" + event.getCategory().name();
		redisTemplate.opsForValue().set(key, dto, duration);
		// 모든 카테고리에 대해 count 증가
		String countKey = "notification:count:user:" + userId + ":" + event.getCategory().name();
		redisTemplate.opsForValue().increment(countKey);
	}

	public void updateNotification(NotificationRedis notification, String key) {
		Duration duration = Duration.ofSeconds(redisTemplate.getExpire(key, TimeUnit.SECONDS));
		NotificationRedis dto = new NotificationRedis(notification.getUuid(), notification.getCategory(),
			notification.getContent(),
			notification.getRedirectUrl(), notification.isRead(), notification.getCreatedAt());
		redisTemplate.opsForValue().set(key, dto, duration);
	}

	public void decreaseCount(String key, Long amount) {
		redisTemplate.opsForValue().decrement(key, amount);
	}

	/**
	 * 사용자가 필요한 get 메서드가 더 있다면 직접 만들어서 사용하세요(형변환 등)
	 */

	public Object getKeyValue(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	public Long getKeyLongValue(String key) {
		Object object = redisTemplate.opsForValue().get(key);
		if (object instanceof Number) {
			return ((Number)object).longValue();
		}
		return null;
	}

	public List<?> getKeyValues(String key) {
		Object object = redisTemplate.opsForValue().get(key);

		if (object instanceof List<?> result) {
			return result;
		}

		return Collections.emptyList();
	}

	public List<String> getKeyStrings(String key) {
		List<String> cached = stringRedisTemplate.opsForList().range(key, 0, -1);
		return cached != null ? cached : Collections.emptyList();
	}

	public Set<Object> getZSetRangeByScore(String key, Long min, Long max) {
		return redisTemplate.opsForZSet().rangeByScore(key, min, max);
	}

	public Set<String> getKeys(String pattern) {
		Set<String> keys = redisTemplate.keys(pattern);
		if (keys != null && !keys.isEmpty()) {
			return keys;
		}
		return Collections.emptySet();
	}
}

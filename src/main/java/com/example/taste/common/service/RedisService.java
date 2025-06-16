package com.example.taste.common.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import com.example.taste.common.constant.RedisChannel;
import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class RedisService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisTemplate<String, String> stringRedisTemplate;
	private final ObjectMapper objectMapper;
	private final GenericJackson2JsonRedisSerializer serializer;

	public RedisService(RedisTemplate<String, Object> redisTemplate,
		RedisTemplate<String, String> stringRedisTemplate,
		ObjectMapper objectMapper, GenericJackson2JsonRedisSerializer serializer) {
		this.redisTemplate = redisTemplate;
		this.stringRedisTemplate = stringRedisTemplate;
		this.objectMapper = objectMapper;
		this.serializer = serializer;
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

	public void addToZSet(String key, Object value, Long score) {
		redisTemplate.opsForZSet().add(key, value, score);
	}

	public void removeFromZSet(String key, Object value) {
		redisTemplate.opsForZSet().remove(key, value);
	}

	public void deleteZSetKey(String key) {
		if (redisTemplate.hasKey(key)) {
			redisTemplate.delete(key);
		}
	}

	//Notification publish
	public void publishNotification(NotificationEventDto event) {
		redisTemplate.convertAndSend(RedisChannel.NOTIFICATION_CHANNEL, event);
	}

	//Notification store
	public void storeNotification(Long userId, Long contentId, NotificationEventDto eventDto,
		Duration duration) {
		eventDto.setContentId(contentId);
		String key = "notification:user:" + userId + ":id:" + contentId + ":" + eventDto.getCategory().name();
		redisTemplate.opsForValue().set(key, eventDto, duration);
		// 모든 카테고리에 대해 count 증가
		String countKey = "notification:count:user:" + userId + ":" + eventDto.getCategory().name();
		redisTemplate.opsForValue().increment(countKey);
	}

	public void updateNotification(NotificationEventDto eventDto, String key) {
		Duration duration = Duration.ofSeconds(redisTemplate.getExpire(key, TimeUnit.SECONDS));
		redisTemplate.opsForValue().set(key, eventDto, duration);
	}

	// 매칭 작업 이벤트 발행
	public void publishMatchEvent(MatchEvent event) {
		redisTemplate.convertAndSend(RedisChannel.MATCH_CHANNEL, event);
	}

	// 알림 카운트 감소
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

	//TODO scan 방식 고려
	public Set<String> getKeys(String pattern) {
		Set<String> keys = redisTemplate.keys(pattern);
		if (keys != null && !keys.isEmpty()) {
			return keys;
		}
		return Collections.emptySet();
	}

	public <T> List<T> getOpsForList(String key, Class<T> clazz) {
		List<Object> objectList = redisTemplate.opsForList().range(key, 0, -1);

		if (objectList == null) {
			return List.of();
		}

		return objectList.stream()
			.map(item -> {
				try {
					return objectMapper.convertValue(item, clazz);
				} catch (Exception e) {
					log.warn("레디스 내 Ojbect -> PkLogCacheDto로 변환 실패");
					return null;
					//CustomException을 날리진 않음
					//convert 실패 처리 하나 때문에 모든 스케줄러 로직이 멈출 순 없으니까
				}
			})
			.filter(Objects::nonNull)
			.toList();
	}

	public long getZSetSize(String key) {
		Long size = redisTemplate.opsForZSet().size(key);
		return size == null ? 0 : size;
	}

	public Long getRank(String key, Object value) {
		return redisTemplate.opsForZSet().rank(key, value);
	}

	public boolean hasRankInZSet(String key, Object value) {
		return redisTemplate.opsForZSet().score(key, value) != null;
	}

	public <T> T deserializeMesageToObject(Message message, Class<T> valueType) {
		return this.serializer.deserialize(message.getBody(), valueType);
	}

	// MEMO : 추후 성능 비교해서 필요한 직렬화 구현체 사용 - @윤예진
	// public <T> T deserializeMessageToObject(Message message, Class<T> valueType) {
	// 	String publishedMessage = redisTemplate.getStringSerializer().deserialize(message.getBody());
	// 	try {
	// 		return objectMapper.readValue(publishedMessage, valueType);
	// 	} catch (JsonProcessingException e) {
	// 		throw new RuntimeException(e);
	// 	}
	// }
}

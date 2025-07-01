package com.example.taste.common.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import com.example.taste.common.constant.RedisChannel;
import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
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

	public void deleteKey(String key) {
		if (redisTemplate.hasKey(key)) {
			redisTemplate.delete(key);
		}
	}

	public void convertAndSend(String channel, NotificationPublishDto publishDto) {
		redisTemplate.convertAndSend(channel, publishDto);
	}

	// 매칭 작업 이벤트 발행
	public void publishMatchEvent(MatchEvent event) {
		redisTemplate.convertAndSend(RedisChannel.MATCH_CHANNEL, event);
	}

	public void decreaseCount(String key) {
		redisTemplate.opsForValue().decrement(key);
	}

	public void increaseCount(String key) {
		redisTemplate.opsForValue().increment(key);
	}

	public void listLeftPush(String key, String value) {
		redisTemplate.opsForList().leftPush(key, value);
	}

	public void delete(String key) {
		redisTemplate.delete(key);
	}

	public <T> void deleteFromList(String key, T value, Class<T> tClass) {
		T serializedValue = objectMapper.convertValue(value, tClass);
		redisTemplate.opsForList().remove(key, 1, serializedValue);
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

	public Set<Object> getZSetRange(String key) {
		return redisTemplate.opsForZSet().range(key, 0, -1);
	}

	//scan 방식으로 변경하였음 -황기하
	public Set<String> getKeys(String pattern) {
		Set<String> keys = new HashSet<>();
		RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

		// 커서를 명시적으로 닫지 않고, 사용이 종료되면 알아서 닫히도록 try 안에서 사용함.
		try (Cursor<byte[]> cursor = connection.keyCommands().scan(
			ScanOptions.scanOptions().match(pattern).count(1000).build()
		)) {
			while (cursor.hasNext()) {
				keys.add(new String(cursor.next(), StandardCharsets.UTF_8));
			}
		}

		if (keys != null && !keys.isEmpty()) {
			return keys;
		}
		return Collections.emptySet();
	}

	public List<String> getKeysFromList(String listKey, int index) {
		List<String> raw = stringRedisTemplate.opsForList().range(listKey, 0, 99);
		if (raw == null || raw.isEmpty()) {
			return Collections.emptyList();
		}
		int here = index * 10;
		int there = here + 11;
		List<String> keyList = new ArrayList<>();
		int count = 0;
		for (String key : raw) {
			NotificationDataDto dataDto = (NotificationDataDto)redisTemplate.opsForValue().get(key);
			if (dataDto == null) {
				redisTemplate.opsForList().remove(listKey, 1, key);
				continue;
			}
			if (count >= here && count < there) {
				keyList.add(key);
			}
			if (count == there) {
				break;
			}
			count++;
		}
		return keyList;
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
					RedisService.log.warn("레디스 내 Ojbect -> PkLogCacheDto로 변환 실패");
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

	public Long getListSize(String key) {
		Long size = redisTemplate.opsForList().size(key);
		return (size == null || size == 0) ? 0L : size;
	}

	// public <T> T getLast(String key, Class<T> tClass) {
	// 	Object obj = redisTemplate.opsForList().getLast(key);
	// 	if (obj == null) {
	// 		return null;
	// 	}
	// 	return objectMapper.convertValue(obj, tClass);
	// }

	public <T> T getLast(String key, Class<T> tClass) {
		Object obj = redisTemplate.opsForList().index(key, -1); // 마지막 요소
		if (obj == null) {
			return null;
		}
		return objectMapper.convertValue(obj, tClass);
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

	public Duration getExpire(String key, TimeUnit timeUnit) {
		return Duration.ofSeconds(redisTemplate.getExpire(key, timeUnit));
	}

	public <T> List<T> getValuesByKeysAsClass(List<String> keys, Class<T> clazz) {
		if (keys.isEmpty()) {
			return Collections.emptyList();
		}

		List<Object> values = redisTemplate.opsForValue().multiGet(keys);

		return values.stream()
			.filter(Objects::nonNull)
			.map(clazz::cast)  // 안전하게 타입 캐스팅
			.toList();
	}
}

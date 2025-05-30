package com.example.taste.common.service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}

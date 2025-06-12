package com.example.taste.domain.match.redis;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import com.example.taste.domain.match.dto.MatchEvent;

@Service
@RequiredArgsConstructor
public class MatchPublisher {
	private final RedisTemplate<String, Object> redisTemplate;

	// 매칭 작업 메세지 발행
	public void publish(ChannelTopic topic, MatchEvent event) {
		redisTemplate.convertAndSend(topic.getTopic(), event);
	}
}

package com.example.taste.domain.match.redis;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.dto.MatchEvent;

@Service
@RequiredArgsConstructor
public class MatchPublisher {
	private final RedisService redisService;

	// 매칭 작업 메세지 발행
	public void publish(MatchEvent event) {
		redisService.publishMatchEvent(event);
	}
}

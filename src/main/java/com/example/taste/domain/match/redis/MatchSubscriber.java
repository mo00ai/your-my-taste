package com.example.taste.domain.match.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.match.service.MatchEngineService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchSubscriber implements MessageListener {
	private final RedisService redisService;
	private final ObjectMapper objectMapper;
	private final MatchEngineService matchEngineService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		MatchEvent event = redisService.deserializeMessageToObject(message, MatchEvent.class);
		// 작업 타입에 따라 메소드 호출
		switch (event.getMatchJobType()) {
			case USER_MATCH -> {
				matchEngineService.runMatchingForUser(event.getUserMatchCondId());
			}
			case PARTY_MATCH -> {
				matchEngineService.runMatchingForParty();
			}
			default -> {
				throw new IllegalArgumentException("올바르지 않은 매칭 작업 타입이 발행 후 소비되었습니다.");
			}
		}
	}
}

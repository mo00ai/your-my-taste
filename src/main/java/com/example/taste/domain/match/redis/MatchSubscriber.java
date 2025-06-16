package com.example.taste.domain.match.redis;

import static com.example.taste.common.exception.ErrorCode.REDIS_OPERATION_FAILED;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.match.dto.MatchEvent;
import com.example.taste.domain.match.service.MatchEngineService;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchSubscriber implements MessageListener {
	private final RedisService redisService;
	private final MatchEngineService matchEngineService;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		MatchEvent event;
		try {
			event = redisService.deserializeMesageToObject(message, MatchEvent.class);
		} catch (RuntimeException e) {
			log.error("매칭 작업 이벤트 역직렬화 실패", e);
			return;
		}
		// 작업 타입에 따라 메소드 호출
		switch (event.getMatchJobType()) {
			case USER_MATCH -> {
				matchEngineService.runMatchingForUser(event.getUserMatchInfoIdList());
			}
			case PARTY_MATCH -> {
				matchEngineService.runMatchingForParty();
			}
			default -> {
				throw new CustomException(REDIS_OPERATION_FAILED, "올바르지 않은 매칭 작업 타입 값이 발행 후 소비되었습니다.");
			}
		}
	}
}

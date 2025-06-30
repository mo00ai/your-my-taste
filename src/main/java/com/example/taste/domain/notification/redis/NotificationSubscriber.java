package com.example.taste.domain.notification.redis;

import java.nio.charset.StandardCharsets;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.redis.strategy.NotificationStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSubscriber implements MessageListener {
	private final NotificationStrategyFactory strategyFactory;
	private final GenericJackson2JsonRedisSerializer serializer;

	// 알림 발행시 자동으로 실행
	@Override
	public void onMessage(Message message, byte[] pattern) {
		// 받은 메시지를 json 스타일로 맵핑
		String json = new String(message.getBody(), StandardCharsets.UTF_8);
		// json 을 역직렬화 하여 dto 로 맵핑
		// redis 은 커스텀 objectMapper 를 사용해 직렬화 하였으므로 역직렬화때도 같은 objectMapper 를 써야 함
		NotificationPublishDto publishDto = serializer.deserialize(message.getBody(), NotificationPublishDto.class);
		// 받은 알림의 카테고리에 따라 다른 동작

		NotificationStrategy strategy = strategyFactory.getStrategy(publishDto.getCategory());
		strategy.handle(publishDto);
	}
}

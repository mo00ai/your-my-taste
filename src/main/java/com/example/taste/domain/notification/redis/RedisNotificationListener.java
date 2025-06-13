package com.example.taste.domain.notification.redis;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisNotificationListener {
	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final NotificationSubscriber notificationSubscriber;

	// @PostConstruct
	// public void registerSubscriber() {
	// 	// redis 에서 message 가 발생하는 것을 듣고 있을 클래스들을 지정
	// 	// message 가 notification 채널에서 발생하는 경우에만 onMessage 메서드 호출
	// 	redisMessageListenerContainer.addMessageListener(
	// 		notificationSubscriber,
	// 		new ChannelTopic(RedisChannel.NOTIFICATION_CHANNEL)
	// 	);
	// }
}

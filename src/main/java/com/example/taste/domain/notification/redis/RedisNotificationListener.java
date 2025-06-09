package com.example.taste.domain.notification.redis;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisNotificationListener {
	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final NotificationSubscriber notificationSubscriber;

	@PostConstruct
	public void registerSubscriber() {
		redisMessageListenerContainer.addMessageListener(
			notificationSubscriber,
			new ChannelTopic(RedisChannel.NOTIFICATION_CHANNEL)
		);
	}
}

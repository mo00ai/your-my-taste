package com.example.taste.domain.recommend.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ErrorMonitoringProducer {

	private final RabbitTemplate rabbitTemplate;

	public void send(Long userId, String message) {
		String payload = userId + "|" + message;
		rabbitTemplate.convertAndSend("recommend-error-queue", payload);
	}
}

package com.example.taste.domain.recommend.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ErrorMonitoringProducer {

	private final RabbitTemplate rabbitTemplate;

	public void send(Long userId, String message) {
		String payload = userId + "|" + message;

		MessageProperties props = new MessageProperties();
		props.setExpiration("600000"); // 10분 TTL

		Message amqpMessage = new Message(payload.getBytes(), props);

		rabbitTemplate.send("recommend-error-queue", amqpMessage);

	}
}

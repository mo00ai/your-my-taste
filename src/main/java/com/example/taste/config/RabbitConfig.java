package com.example.taste.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	public static final String ERROR_QUEUE_NAME = "recommend-error-queue";

	@Bean
	public Queue recommendErrorQueue() {

		Map<String, Object> args = new HashMap<>();
		args.put("x-message-ttl", 600_000); // mq에 ttl적용해서 최근 10분간의 에러만 mq에 저장될수있도록

		return new Queue(ERROR_QUEUE_NAME, true, false, false, args);
	}

	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}
}

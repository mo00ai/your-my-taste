package com.example.taste.config;

import static com.example.taste.common.constant.RabbitConst.*;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(EXCHANGE_NAME);
	}

	@Bean
	public Queue queue() {
		return new Queue(QUEUE_NAME, true);
	}

	@Bean
	public Queue recommendErrorQueue() {
		return new Queue(ERROR_QUEUE_NAME, true);
	}

	@Bean
	public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
		return new RabbitAdmin(connectionFactory);
	}

	@Bean
	public Binding binding(Queue queue, TopicExchange exchange) {
		return BindingBuilder.bind(queue)
			.to(exchange)
			.with("board.*");
	}

	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}
}

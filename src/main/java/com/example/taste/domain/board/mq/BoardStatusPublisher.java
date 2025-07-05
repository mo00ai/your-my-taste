package com.example.taste.domain.board.mq;

import static com.example.taste.common.constant.RabbitConst.*;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.taste.domain.board.dto.mq.BoardStatusDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardStatusPublisher {

	private final RabbitTemplate rabbitTemplate;

	public void publish(BoardStatusDto dto) {
		String routingKey = "board." + dto.getBoardId();
		rabbitTemplate.convertAndSend(EXCHANGE_NAME, routingKey, dto);
	}
}

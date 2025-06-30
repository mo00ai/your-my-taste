package com.example.taste.domain.board.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.taste.common.service.RedisService;
import com.example.taste.config.RabbitConfig;
import com.example.taste.domain.board.dto.mq.BoardStatusDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardStatusConsumer {
	private final SimpMessagingTemplate messagingTemplate;
	private final RedisService redisService;

	@RabbitListener(queues = RabbitConfig.QUEUE_NAME, concurrency = "3-10")
	public void handleBoardStatus(BoardStatusDto dto) {
		String destination = "/sub/openrun/board/" + dto.getBoardId();
		messagingTemplate.convertAndSend(destination, dto.getRemainingSlot());
	}
}

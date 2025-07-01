package com.example.taste.domain.board.mq;

import static com.example.taste.common.constant.RabbitConst.*;
import static com.example.taste.common.constant.SocketConst.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.dto.mq.BoardStatusDto;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BoardStatusConsumer {
	private final SimpMessagingTemplate messagingTemplate;
	private final RedisService redisService;

	@RabbitListener(queues = QUEUE_NAME, concurrency = "3-10")
	public void handleBoardStatus(BoardStatusDto dto) {
		String destination = BOARD_SOCKET_DESTINATION + dto.getBoardId();
		messagingTemplate.convertAndSend(destination, dto.getRemainingSlot());
	}
}

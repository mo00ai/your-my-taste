package com.example.taste.common.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import com.example.taste.common.websocket.strategy.StompCommandDispatcher;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompCommandInterceptor implements ChannelInterceptor {
	// STOMP 통신 중 메시지 전송과 관련한 추가 로직을 처리할 때 사용
	private final StompCommandDispatcher dispatcher;

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);

		StompCommand command = headerAccessor.getCommand();
		if (command == null) {
			return message;
		}

		return dispatcher.dispatch(headerAccessor, message);
	}
}

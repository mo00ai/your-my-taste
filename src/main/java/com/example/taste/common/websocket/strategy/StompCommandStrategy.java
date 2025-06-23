package com.example.taste.common.websocket.strategy;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public interface StompCommandStrategy {
	boolean supports(StompCommand command);

	Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message);
}
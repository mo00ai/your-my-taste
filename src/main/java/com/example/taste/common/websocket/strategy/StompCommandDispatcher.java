package com.example.taste.common.websocket.strategy;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class StompCommandDispatcher {
	private final List<StompCommandStrategy> strategies;

	public StompCommandDispatcher(List<StompCommandStrategy> strategies) {
		this.strategies = strategies;
	}

	public Message<?> dispatch(StompHeaderAccessor headerAccessor, Message<?> message) {
		for (StompCommandStrategy strategy : strategies) {
			if (strategy.supports(headerAccessor.getCommand())) {
				return strategy.handle(headerAccessor, message);
			}
		}
		return message;
	}
}

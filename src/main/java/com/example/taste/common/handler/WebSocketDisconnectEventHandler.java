package com.example.taste.common.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.taste.common.websocket.strategy.DisconnectCommandStrategy;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectEventHandler {
	private final DisconnectCommandStrategy disconnectStrategy;

	@EventListener
	public void handleDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
		disconnectStrategy.handle(headerAccessor, event.getMessage());
	}

}

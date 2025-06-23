package com.example.taste.common.websocket.strategy;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.taste.common.websocket.manager.WebSocketSubscriptionManager;
import com.example.taste.config.security.CustomUserDetails;

@Component
@RequiredArgsConstructor
public class UnsubscribeCommandStrategy implements StompCommandStrategy {
	private final WebSocketSubscriptionManager subscriptionManager;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.UNSUBSCRIBE.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {
		Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;
		CustomUserDetails userDetails = (CustomUserDetails)(auth != null ? auth.getPrincipal() : null);
		String destination = headerAccessor.getDestination();

		// 인증 정보 없으면 무시
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}

		subscriptionManager.remove(userDetails.getId(), destination);
		return message;
	}
}

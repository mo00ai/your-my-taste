package com.example.taste.common.websocket.strategy;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.taste.common.websocket.manager.OpenRunPerformanceManager;
import com.example.taste.common.websocket.manager.WebSocketSubscriptionManager;
import com.example.taste.config.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisconnectCommandStrategy implements StompCommandStrategy {
	private final WebSocketSubscriptionManager subscriptionManager;
	private final OpenRunPerformanceManager performanceManager;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.DISCONNECT.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {
		Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;
		CustomUserDetails userDetails = (CustomUserDetails)(auth != null ? auth.getPrincipal() : null);

		// 인증 정보 없으면 무시
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}

		subscriptionManager.clear(userDetails.getId());        // 유저의 구독 초기화
		//performanceManager.remove(userDetails.getId());  // 전체 도메인 기준 커넥션 수 관리
		return message;
	}
}

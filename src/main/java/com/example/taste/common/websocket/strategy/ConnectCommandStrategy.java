package com.example.taste.common.websocket.strategy;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.common.websocket.manager.OpenRunPerformanceManager;
import com.example.taste.config.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

//@Component // 전체 도메인 기준 커넥션 수 관리할 때 사용
@RequiredArgsConstructor
public class ConnectCommandStrategy implements StompCommandStrategy {
	private final OpenRunPerformanceManager performanceManager;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.CONNECT.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {

		Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;
		CustomUserDetails userDetails = (CustomUserDetails)(auth != null ? auth.getPrincipal() : null);

		if (auth == null || !auth.isAuthenticated()) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

		// performanceManager에 세션 저장
		performanceManager.add(userDetails.getId());

		return null;
	}
}

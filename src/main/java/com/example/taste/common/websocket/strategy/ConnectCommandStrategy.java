package com.example.taste.common.websocket.strategy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.common.websocket.manager.WebSocketPerformanceManager;
import com.example.taste.config.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConnectCommandStrategy implements StompCommandStrategy {
	private static final Pattern BOARD_CONNECT_PATTERN_COMPILED = Pattern.compile("^/sub/openrun/board/(\\d+)$");
	private final WebSocketPerformanceManager performanceManager;

	@Override
	public boolean supports(StompCommand command) {
		return StompCommand.CONNECT.equals(command);
	}

	@Override
	public Message<?> handle(StompHeaderAccessor headerAccessor, Message<?> message) {
		String destination = headerAccessor.getDestination();
		if (destination == null) {
			return message;
		}

		Matcher boardMatcher = BOARD_CONNECT_PATTERN_COMPILED.matcher(destination);
		Authentication auth = (headerAccessor.getUser() instanceof Authentication a) ? a : null;
		CustomUserDetails userDetails = (CustomUserDetails)(auth != null ? auth.getPrincipal() : null);

		if (auth == null || !auth.isAuthenticated()) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}

		if (boardMatcher.matches()) {
			// performanceManager에 세션 저장해서 세션 수 관리
		}

		return null;
	}
}

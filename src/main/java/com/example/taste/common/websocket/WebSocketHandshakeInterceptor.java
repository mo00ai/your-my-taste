package com.example.taste.common.websocket;

import static com.example.taste.common.exception.ErrorCode.INVALID_SIGNATURE;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.taste.common.exception.CustomException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpSession session = servletRequest.getServletRequest().getSession(false);

			// 세션이 없다면 인증(로그인) 필요
			if (session == null) {
				throw new CustomException(INVALID_SIGNATURE);
			}
			SecurityContext securityContext = (SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT");
			if (securityContext == null || securityContext.getAuthentication() == null) {
				throw new CustomException(INVALID_SIGNATURE);
			}

			Authentication authentication = securityContext.getAuthentication();
			attributes.put("user", authentication.getPrincipal());
			// attributes.put("sessionId", session.getId());
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
		if (exception != null) {
			log.error("WebSocket 핸드셰이크 실패", exception);
		}
	}
}

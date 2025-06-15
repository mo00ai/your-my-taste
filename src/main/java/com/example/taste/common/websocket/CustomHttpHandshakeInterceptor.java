package com.example.taste.common.websocket;

import static com.example.taste.common.exception.ErrorCode.INVALID_SIGNATURE;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.example.taste.common.exception.CustomException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class CustomHttpHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		super.beforeHandshake(request, response, wsHandler, attributes);
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpRequest = servletRequest.getServletRequest();
			HttpSession session = httpRequest.getSession(false);

			if (session == null) {
				throw new CustomException(INVALID_SIGNATURE);
			}

			SecurityContext context = (SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT");
			if (context == null || context.getAuthentication() == null
				|| !context.getAuthentication().isAuthenticated()) {
				throw new CustomException(INVALID_SIGNATURE);
			}

			// 필요 시 사용자 정보 저장
			attributes.put("user", context.getAuthentication().getPrincipal());
			log.info("핸드쉐이킹 성공 UserID: {}", context.getAuthentication().getPrincipal());
			return true;
		}

		return false;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
	}
}

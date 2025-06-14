package com.example.taste.common.websocket;

import static com.example.taste.common.exception.ErrorCode.INVALID_SIGNATURE;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
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
		Object session = attributes.get("SPRING_SECURITY_CONTEXT");

		// 세션이 없다면 인증(로그인) 필요
		if (session == null) {
			throw new CustomException(INVALID_SIGNATURE);
		}

		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Exception exception) {
	}
}

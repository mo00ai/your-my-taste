package com.example.taste.common.interceptor;

import static com.example.taste.domain.auth.exception.AuthErrorCode.UNAUTHENTICATED;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

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
public class CustomHttpHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
		WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
		super.beforeHandshake(request, response, wsHandler, attributes);
		if (request instanceof ServletServerHttpRequest servletRequest) {
			HttpServletRequest httpRequest = servletRequest.getServletRequest();
			HttpSession session = httpRequest.getSession(false);

			if (session == null) {
				throw new CustomException(UNAUTHENTICATED);
			}

			SecurityContext context = (SecurityContext)session.getAttribute(SPRING_SECURITY_CONTEXT_KEY);
			if (context == null || context.getAuthentication() == null
				|| !context.getAuthentication().isAuthenticated()) {
				throw new CustomException(UNAUTHENTICATED);
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

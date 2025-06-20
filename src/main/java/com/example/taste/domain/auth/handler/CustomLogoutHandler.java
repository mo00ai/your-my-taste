package com.example.taste.domain.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import com.example.taste.common.service.RedisService;

@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
	private final RedisService redisService;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		HttpSession session = request.getSession(false);

		if (session != null) {
			String sessionId = session.getId();

			// 세션 키 삭제
			String redisSessionKey = "spring:session:sessions:" + sessionId;
			redisService.deleteZSetKey(redisSessionKey);

			session.invalidate();                        // 로컬 세션 무효화
			SecurityContextHolder.clearContext();        // Security Context 초기화
		}
	}
}

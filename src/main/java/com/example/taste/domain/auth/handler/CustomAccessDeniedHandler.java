package com.example.taste.domain.auth.handler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.auth.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	private final ObjectMapper objectMapper;

	// 인가 예외 핸들러
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		AuthErrorCode errorCode = AuthErrorCode.UNAUTHORIZED;

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		CommonResponse<?> body = CommonResponse.error(
			errorCode.getHttpStatus(),
			errorCode.getCode(),
			errorCode.getMessage()
		);

		String json = objectMapper.writeValueAsString(body);
		response.getWriter().write(json);
	}
}

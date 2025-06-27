package com.example.taste.domain.auth.handler;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.auth.exception.AuthErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper;

	// 인증 예외 핸들러
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException, ServletException {
		AuthErrorCode errorCode = AuthErrorCode.UNAUTHENTICATED;

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType("application/json;charset=UTF-8");

		CommonResponse<?> body = CommonResponse.error(
			errorCode.getHttpStatus(),
			errorCode.getCode(),
			errorCode.getMessage()
		);

		String json = objectMapper.writeValueAsString(body);
		response.getWriter().write(json);
	}
}

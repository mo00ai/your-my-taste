package com.example.taste.domain.auth.filter;

import static com.example.taste.common.exception.ErrorCode.INVALID_INPUT_VALUE;
import static com.example.taste.common.exception.ErrorCode.METHOD_NOT_ALLOWED;
import static com.example.taste.domain.auth.exception.AuthErrorCode.ALREADY_LOGIN;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class UserAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private final ObjectMapper objectMapper;

	public UserAuthenticationFilter(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
		throws AuthenticationException {
		// POST 요청이 아니라면
		if (!request.getMethod().equals("POST")) {
			throw new CustomException(METHOD_NOT_ALLOWED);
		}

		// 로그인(기존 세션) 확인
		HttpSession session = request.getSession(false);
		SecurityContext securityContext = (session != null) ?
			(SecurityContext)session.getAttribute(SPRING_SECURITY_CONTEXT_KEY) : null;

		if (securityContext != null) {
			Authentication auth = securityContext.getAuthentication();
			if (auth != null && auth.isAuthenticated()) {
				throw new CustomException(ALREADY_LOGIN);
			}
		}
		try {
			SigninRequestDto loginRequest = objectMapper.readValue(
				request.getInputStream(), SigninRequestDto.class);

			UsernamePasswordAuthenticationToken authRequest =
				new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

			return this.getAuthenticationManager().authenticate(authRequest);
		} catch (IOException e) {
			throw new CustomException(INVALID_INPUT_VALUE, "로그인 요청 값 파싱 중 오류가 발생하였습니다.");
		}
	}

	@Override
	protected void successfulAuthentication(
		HttpServletRequest request, HttpServletResponse response,
		FilterChain chain, Authentication auth) throws IOException, ServletException {
		// 인증 정보 저장
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);

		// 세션에 스프링 시큐리티 컨텍스트 세팅
		request.getSession(true).setAttribute(
			SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
		SecurityContextHolder.setContext(context);

		// 로그인 성공 응답
		response.setStatus(HttpStatus.OK.value());
		response.setContentType("application/json;charset=UTF-8");

		String json = objectMapper.writeValueAsString(CommonResponse.ok());
		response.getWriter().write(json);
	}
}

package com.example.taste.domain.auth.service;

import static com.example.taste.domain.auth.exception.AuthErrorCode.ALREADY_LOGIN;
import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.user.entity.CustomUserDetails;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void signin(HttpServletRequest httpRequest, SigninRequestDto requestDto) {
		// 로그인(기존 세션) 확인
		HttpSession session = httpRequest.getSession(false);

		SecurityContext securityContext = (session != null) ?
			(SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT") : null;

		if (securityContext != null) {
			Authentication auth = securityContext.getAuthentication();
			if (auth != null && auth.isAuthenticated()) {
				throw new CustomException(ALREADY_LOGIN);
			}
		}

		// 이메일 검증
		User user = userRepository.findUserByEmail(requestDto.getEmail()).orElseThrow(
			() -> new CustomException(NOT_FOUND_USER));

		// 탈퇴한 사용자 검증
		if (user.getDeletedAt() != null) {
			throw new CustomException(DEACTIVATED_USER);
		}

		// 비밀번호 검증
		if (!isValidPassword(requestDto.getPassword(), user.getPassword())) {
			throw new CustomException(INVALID_PASSWORD);
		}

		// 인증 정보 저장
		CustomUserDetails userDetails = new CustomUserDetails(user);
		Authentication auth =
			new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(auth);
		SecurityContextHolder.setContext(context);
		httpRequest.getSession(true)
			.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
	}

	public void signout(HttpServletRequest httpRequest, CustomUserDetails userDetails) {
		HttpSession session = httpRequest.getSession(false);

		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
	}

	// 비밀번호 일치 검사
	private boolean isValidPassword(String rawPassword, String encodePassword) {
		return passwordEncoder.matches(rawPassword, encodePassword);
	}
}

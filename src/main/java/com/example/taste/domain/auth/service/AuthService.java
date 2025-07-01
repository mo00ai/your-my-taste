package com.example.taste.domain.auth.service;

import static com.example.taste.domain.user.exception.UserErrorCode.DEACTIVATED_USER;
import static com.example.taste.domain.user.exception.UserErrorCode.INVALID_PASSWORD;
import static com.example.taste.domain.user.exception.UserErrorCode.NOT_FOUND_USER;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import com.example.taste.domain.user.dto.UserSigninProjectionDto;
import com.example.taste.domain.user.entity.CustomUserDetails;
import com.example.taste.domain.user.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public void signin(HttpServletRequest httpRequest, SigninRequestDto requestDto) {
		// 로그인(기존 세션) 확인
		HttpSession oldSession = httpRequest.getSession(false);
		if (oldSession != null) {
			oldSession.invalidate();    // 기존 세션 무효화
			SecurityContextHolder.clearContext(); // 기존 인증 정보 초기화
		}

		// 이메일 검증
		// User user = userRepository.findUserByEmail(requestDto.getEmail()).orElseThrow(
		// 	() -> new CustomException(NOT_FOUND_USER));

		UserSigninProjectionDto user = userRepository.findSigninProjectionByEmail(requestDto.getEmail())
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));

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

		// 새 세션 생성 후 저장
		httpRequest.getSession(true)
			.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
	}

	public void signout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		HttpSession session = httpRequest.getSession(false);

		if (session != null) {
			session.invalidate();
		}

		SecurityContextHolder.clearContext();

		// 쿠키 삭제
		Cookie cookie = new Cookie("JSESSIONID", null);
		cookie.setPath("/");
		cookie.setMaxAge(0);
		httpResponse.addCookie(cookie);
	}

	// 비밀번호 일치 검사
	private boolean isValidPassword(String rawPassword, String encodePassword) {
		return passwordEncoder.matches(rawPassword, encodePassword);
	}
}

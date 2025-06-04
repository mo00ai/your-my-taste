package com.example.taste.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.auth.dto.SignupRequestDto;
import com.example.taste.domain.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signup")
	public CommonResponse<Void> signup(
		@RequestPart SignupRequestDto requestDto, @RequestPart(required = false) MultipartFile file) {
		authService.signup(requestDto, file);
		return CommonResponse.ok();
	}

	@PostMapping("/signin")
	public CommonResponse<Void> signin(
		HttpServletRequest httpRequest, @RequestBody SigninRequestDto requestDto) {
		authService.signin(httpRequest, requestDto);

		return CommonResponse.ok();
	}

	@PostMapping("/signout")
	public CommonResponse<Void> signout(
		HttpServletRequest httpRequest, @AuthenticationPrincipal CustomUserDetails userDetails) {
		authService.signout(httpRequest, userDetails);

		return CommonResponse.ok();
	}
}

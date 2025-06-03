package com.example.taste.domain.auth.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.auth.dto.SignUpRequestDto;
import com.example.taste.domain.auth.dto.SigninRequestDto;
import com.example.taste.domain.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/signup")
	public CommonResponse<Void> signup(
		@RequestPart SignUpRequestDto requestDto, @RequestPart(required = false) MultipartFile file) {
		authService.signup(requestDto, file);
		return CommonResponse.ok();
	}

	@PostMapping("/login")
	public CommonResponse<Void> login(@RequestBody SigninRequestDto requestDto) {
		authService.login(requestDto);
		return CommonResponse.ok();
	}
}

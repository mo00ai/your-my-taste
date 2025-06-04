package com.example.taste.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;

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

	// TODO: 로그인 시 파라미터가 json body 로 넘어가는지 아니면 requestParam 으로 넘겨줘야하는지 확인필요
	// TODO: 혹은 로그인 실패 핸들러를 추가해야하는지?
	@PostMapping("/signin")
	public CommonResponse<Void> login(
		HttpServletRequest httpRequest, @RequestBody SigninRequestDto requestDto) {
		authService.login(httpRequest, requestDto);
		
		return CommonResponse.ok();
	}
}

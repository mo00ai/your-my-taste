package com.example.taste.domain.user.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.user.dto.UserMyProfileResponseDto;
import com.example.taste.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// TODO: 인증/인가 추가
	@GetMapping("/users")
	public CommonResponse<UserMyProfileResponseDto> getMyProfile() {
		Long userId = 1L;
		return CommonResponse.ok(userService.getMyProfile(1L));
	}

	@GetMapping("/users/{userId}")
	public CommonResponse<UserMyProfileResponseDto> getProfile(
		@PathVariable Long userId) {
		return CommonResponse.ok(userService.getProfile(userId));
	}
}

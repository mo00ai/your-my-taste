package com.example.taste.domain.user.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.user.dto.request.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateListRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.dto.response.UserMyProfileResponseDto;
import com.example.taste.domain.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	// TODO: 인증/인가 추가
	@GetMapping
	public CommonResponse<UserMyProfileResponseDto> getMyProfile() {
		Long userId = 1L;
		return CommonResponse.ok(userService.getMyProfile(1L));
	}

	@GetMapping("/{userId}")
	public CommonResponse<UserMyProfileResponseDto> getProfile(
		@PathVariable Long userId) {
		return CommonResponse.ok(userService.getProfile(userId));
	}

	@PatchMapping
	public CommonResponse<Void> updateProfile(
		@RequestBody UserUpdateRequestDto requestDto) {
		Long userId = 1L;
		userService.updateUser(userId, requestDto);
		return CommonResponse.ok();
	}

	@DeleteMapping
	public CommonResponse<Void> deleteUser(
		@RequestBody UserDeleteRequestDto requestDto) {
		Long userId = 1L;
		userService.deleteUser(userId, requestDto);
		return CommonResponse.ok();
	}

	@PostMapping("/favor")
	public CommonResponse<Void> updateUserFavor(
		@RequestBody UserFavorUpdateListRequestDto requestDto
	) {
		Long userId = 1L;
		userService.updateUserFavor(userId, requestDto);
		return CommonResponse.ok();
	}

	@PostMapping("/{followingUserId}/follow")
	public CommonResponse<Void> followUser(
		@RequestParam Long followingUserId
	) {
		Long userId = 1L;
		userService.followUser(userId, followingUserId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{followingUserId}/follow")
	public CommonResponse<Void> unfollowUser(
		@RequestParam Long followingUserId
	) {
		Long userId = 1L;
		userService.unfollowUser(userId, followingUserId);
		return CommonResponse.ok();
	}
}

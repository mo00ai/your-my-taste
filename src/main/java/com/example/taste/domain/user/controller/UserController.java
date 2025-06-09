package com.example.taste.domain.user.controller;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.annotation.ImageValid;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.user.dto.request.UserDeleteRequestDto;
import com.example.taste.domain.user.dto.request.UserFavorUpdateRequestDto;
import com.example.taste.domain.user.dto.request.UserUpdateRequestDto;
import com.example.taste.domain.user.dto.response.UserMyProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserProfileResponseDto;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;
import com.example.taste.domain.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;

	@GetMapping
	public CommonResponse<UserMyProfileResponseDto> getMyProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(userService.getMyProfile(userDetails.getId()));
	}

	@GetMapping("/{userId}")
	public CommonResponse<UserProfileResponseDto> getProfile(
		@PathVariable Long userId) {
		return CommonResponse.ok(userService.getProfile(userId));
	}

	@ImageValid
	@PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CommonResponse<Void> updateProfile(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestPart(name = "data") @Valid UserUpdateRequestDto requestDto,
		@RequestPart(name = "file", required = false) MultipartFile file) {
		userService.updateUser(userDetails.getId(), requestDto, file);
		return CommonResponse.ok();
	}

	@DeleteMapping
	public CommonResponse<Void> deleteUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid UserDeleteRequestDto requestDto) {
		userService.deleteUser(userDetails.getId(), requestDto);
		return CommonResponse.ok();
	}

	@PostMapping("/favor")
	public CommonResponse<Void> updateUserFavor(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid List<UserFavorUpdateRequestDto> requestDtoList
	) {
		userService.updateUserFavors(userDetails.getId(), requestDtoList);
		return CommonResponse.ok();
	}

	@GetMapping("/{userId}/following")
	public CommonResponse<List<UserSimpleResponseDto>> getFollowingUsers(
		@PathVariable Long userId
	) {
		return CommonResponse.ok(userService.getFollowingUserList(userId));
	}

	@GetMapping("/{userId}/followers")
	public CommonResponse<List<UserSimpleResponseDto>> getFollowerUsers(
		@PathVariable Long userId
	) {
		return CommonResponse.ok(userService.getFollowerUserList(userId));
	}

	@PostMapping("/{followingUserId}/follow")
	public CommonResponse<Void> followUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long followingUserId
	) {
		userService.followUser(userDetails.getId(), followingUserId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{followingUserId}/follow")
	public CommonResponse<Void> unfollowUser(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long followingUserId
	) {
		userService.unfollowUser(userDetails.getId(), followingUserId);
		return CommonResponse.ok();
	}
}

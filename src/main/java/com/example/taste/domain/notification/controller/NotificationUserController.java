package com.example.taste.domain.notification.controller;

import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationResponseDto;
import com.example.taste.domain.notification.service.NotificationUserService;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationUserController {

	private final NotificationUserService notificationUserService;

	// 알림 카운트 접근(전체)(원하지 않는 카테고리 알림 삭제)
	@GetMapping
	public CommonResponse<GetNotificationCountResponseDto> getNotificationCount(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(notificationUserService.getNotificationCount(userDetails));
	}

	// 알림 목록 접근
	@GetMapping("/list")
	public CommonResponse<Slice<NotificationResponseDto>> getNotificationList(
		@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(defaultValue = "1") @Min(1) int index) {
		return CommonResponse.ok(notificationUserService.getNotificationList(userDetails, index));
	}

	// 알림 추가 접근
	@GetMapping("/list/old")
	public CommonResponse<Slice<NotificationResponseDto>> getOldNotificationList(
		@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(defaultValue = "1") @Min(1) int index) {
		return CommonResponse.ok(notificationUserService.getMoreNotificationList(userDetails, index));
	}

	// 알림 읽음 처리하기
	@PatchMapping
	public CommonResponse<Void> markNotificationAsRead(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam Long contentsId) {
		notificationUserService.markNotificationAsRead(userDetails, contentsId);
		return CommonResponse.ok();
	}

	// 전체 알림 읽음 처리하기
	@PatchMapping("/all")
	public CommonResponse<Void> markAllNotificationAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
		notificationUserService.markAllNotificationAsRead(userDetails);
		return CommonResponse.ok();
	}
}

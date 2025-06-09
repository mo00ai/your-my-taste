package com.example.taste.domain.notification.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationRedis;
import com.example.taste.domain.notification.service.NotificationUserService;

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
	@GetMapping("/notifications")
	public CommonResponse<List<NotificationRedis>> getNotificationList(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(notificationUserService.getNotificationList(userDetails));
	}

	// 알림 읽음 처리하기
	@PatchMapping
	public CommonResponse<Void> markNotificationAsRead(@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam Long uuid) {
		notificationUserService.markNotificationAsRead(userDetails, uuid);
		return CommonResponse.ok();
	}
}

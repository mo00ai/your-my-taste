package com.example.taste.domain.notification.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.notification.dto.AdminNotificationRequestDto;
import com.example.taste.domain.notification.service.NotificationAdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications/admin")
public class NotificationAdminController {
	private final NotificationAdminService notificationAdminService;

	// 관리자용 알림 생성
	// 마케팅, 시스템 알림 생성 가능
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public CommonResponse<Void> publishNotification(@RequestBody @Valid AdminNotificationRequestDto dto) {
		notificationAdminService.publishNotification(dto);
		return CommonResponse.ok();
	}

	// TODO 개인에게 알림 보내는 기능 추가할 것.
}

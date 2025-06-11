package com.example.taste.domain.notification.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.notification.dto.NotificationRequestDto;
import com.example.taste.domain.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notification/admin")
public class NotificationAdminController {
	private final NotificationService notificationService;

	@PostMapping
	public CommonResponse<Void> publishNotification(@RequestBody NotificationRequestDto dto) {
		notificationService.publishNotification(dto);
		return CommonResponse.ok();
	}
}

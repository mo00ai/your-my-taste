package com.example.taste.domain.notification.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.notification.dto.PushSubscribeRequestDto;
import com.example.taste.domain.notification.service.WebPushService;
import com.example.taste.domain.user.entity.CustomUserDetails;

@RestController
@RequestMapping("/web-push")
@RequiredArgsConstructor
public class WebPushController {

	private final WebPushService webPushService;

	@PostMapping("/subscribe")
	public CommonResponse<Void> subscribe(
		@RequestBody PushSubscribeRequestDto dto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		webPushService.saveSubscription(userDetails.getId(), dto);
		return CommonResponse.ok();
	}

	@DeleteMapping("/unsubscribe")
	public CommonResponse<Void> unsubscribe(
		@RequestBody PushSubscribeRequestDto dto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		webPushService.deleteSubscription(userDetails.getId(), dto.getFcmToken());
		return CommonResponse.ok();
	}

}

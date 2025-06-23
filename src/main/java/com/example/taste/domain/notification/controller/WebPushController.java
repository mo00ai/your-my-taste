package com.example.taste.domain.notification.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.notification.dto.PushSubscribeRequestDto;
import com.example.taste.domain.notification.service.WebPushService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class WebPushController {

	private final WebPushService webPushService;

	@PostMapping("/subscribe")
	public CommonResponse<Void> subscribe(
		@RequestBody PushSubscribeRequestDto dto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		webPushService.saveSubscription(userDetails.getUser(), dto);
		return CommonResponse.ok();
	}

}

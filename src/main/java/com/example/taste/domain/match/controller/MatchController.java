package com.example.taste.domain.match.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.match.dto.UserMatchCondCreateRequestDto;
import com.example.taste.domain.match.service.MatchService;

@RequestMapping
@RestController
@RequiredArgsConstructor
public class MatchController {
	private final MatchService matchService;

	@PostMapping("/matches")
	public CommonResponse<Void> startUserMatch(
		@RequestParam Long prefId, @AuthenticationPrincipal CustomUserDetails userDetails) {
		matchService.startUserMatch(prefId);
		return CommonResponse.ok();
	}

	@PostMapping("/match-conditions")
	public CommonResponse<Void> createUserMatchCond(
		@RequestBody UserMatchCondCreateRequestDto requestDto,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		matchService.startUserMatch(userDetails.getId());
		return CommonResponse.ok();
	}
}

package com.example.taste.domain.match.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.match.dto.request.UserMatchCondCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchCondUpdateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchCondResponseDto;
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
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody UserMatchCondCreateRequestDto requestDto) {
		matchService.createUserMatchCond(userDetails.getId(), requestDto);
		return CommonResponse.ok();
	}

	@GetMapping("/match-conditions")
	public CommonResponse<List<UserMatchCondResponseDto>> getUserMatchCond(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(matchService.findUserMatchCond(userDetails.getId()));
	}

	@PatchMapping("/match-conditions/{matchingConditionId}")
	public CommonResponse<Void> updateUserMatchCond(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long matchingConditionId,
		@RequestBody UserMatchCondUpdateRequestDto requestDto) {
		matchService.updateUserMatchCond(userDetails.getId(), matchingConditionId, requestDto);
		return CommonResponse.ok();
	}
}

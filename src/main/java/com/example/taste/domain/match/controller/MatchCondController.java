package com.example.taste.domain.match.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.match.dto.request.UserMatchCondCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchCondUpdateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchCondResponseDto;
import com.example.taste.domain.match.service.MatchCondService;

@RequestMapping("/match-conditions")
@RestController
@RequiredArgsConstructor
public class MatchCondController {
	private final MatchCondService matchCondService;

	@PostMapping
	public CommonResponse<Void> createUserMatchCond(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody UserMatchCondCreateRequestDto requestDto) {
		matchCondService.createUserMatchCond(userDetails.getId(), requestDto);
		return CommonResponse.ok();
	}

	@GetMapping
	public CommonResponse<List<UserMatchCondResponseDto>> getUserMatchCond(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(matchCondService.findUserMatchCond(userDetails.getId()));
	}

	@PatchMapping("/{matchingConditionId}")
	public CommonResponse<Void> updateUserMatchCond(
		@PathVariable Long matchingConditionId,
		@RequestBody UserMatchCondUpdateRequestDto requestDto) {
		matchCondService.updateUserMatchCond(matchingConditionId, requestDto);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{matchingConditionId}")
	public CommonResponse<Void> updateUserMatchCond(
		@PathVariable Long matchingConditionId) {
		matchCondService.deleteUserMatchCond(matchingConditionId);
		return CommonResponse.ok();
	}
}

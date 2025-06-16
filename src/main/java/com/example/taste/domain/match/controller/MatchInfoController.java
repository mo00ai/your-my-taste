package com.example.taste.domain.match.controller;

import java.util.List;

import jakarta.validation.Valid;

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
import com.example.taste.domain.match.dto.request.UserMatchInfoCreateRequestDto;
import com.example.taste.domain.match.dto.request.UserMatchInfoUpdateRequestDto;
import com.example.taste.domain.match.dto.response.UserMatchInfoResponseDto;
import com.example.taste.domain.match.service.MatchInfoService;

@RequestMapping("/match-infos")
@RestController
@RequiredArgsConstructor
public class MatchInfoController {
	private final MatchInfoService matchInfoService;

	@PostMapping("/users")
	public CommonResponse<Void> createUserMatchInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid UserMatchInfoCreateRequestDto requestDto) {
		matchInfoService.createUserMatchInfo(userDetails.getId(), requestDto);
		return CommonResponse.ok();
	}

	@GetMapping("/users")
	public CommonResponse<List<UserMatchInfoResponseDto>> getUserMatchInfo(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(matchInfoService.findUserMatchInfo(userDetails.getId()));
	}

	@PatchMapping("/users/{matchInfoId}")
	public CommonResponse<Void> updateUserMatchInfo(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable Long matchInfoId,
		@RequestBody @Valid UserMatchInfoUpdateRequestDto requestDto) {
		matchInfoService.updateUserMatchInfo(customUserDetails.getUser(), matchInfoId, requestDto);
		return CommonResponse.ok();
	}

	@DeleteMapping("/users/{matchInfoId}")
	public CommonResponse<Void> deleteUserMatchInfo(
		@AuthenticationPrincipal CustomUserDetails customUserDetails,
		@PathVariable Long matchInfoId) {
		matchInfoService.deleteUserMatchInfo(customUserDetails.getUser(), matchInfoId);
		return CommonResponse.ok();
	}
}

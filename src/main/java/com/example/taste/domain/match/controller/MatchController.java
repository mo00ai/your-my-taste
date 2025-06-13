package com.example.taste.domain.match.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.match.dto.request.PartyMatchCondCreateRequestDto;
import com.example.taste.domain.match.service.MatchService;

@RequestMapping("/matches")
@RestController
@RequiredArgsConstructor
public class MatchController {
	private final MatchService matchService;

	@PostMapping("/users/register")
	public CommonResponse<Void> registerUserMatch(@RequestParam Long userMatchInfoId) {
		matchService.registerUserMatch(userMatchInfoId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/users/cancel")
	public CommonResponse<Void> cancelUserMatch(@RequestParam Long userMatchInfoId) {
		matchService.cancelUserMatch(userMatchInfoId);
		return CommonResponse.ok();
	}

	@PostMapping("/parties/register")
	public CommonResponse<Void> registerPartyMatch(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody PartyMatchCondCreateRequestDto requestDto) {
		matchService.registerPartyMatch(userDetails.getId(), requestDto);
		return CommonResponse.ok();
	}

	@DeleteMapping("/parties/cancel")
	public CommonResponse<Void> cancelPartyMatch(
		@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam Long partyId) {
		matchService.cancelPartyMatch(userDetails.getId(), partyId);
		return CommonResponse.ok();
	}
}

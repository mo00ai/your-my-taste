package com.example.taste.domain.match.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.match.dto.request.PartyMatchCondCreateRequestDto;
import com.example.taste.domain.match.service.MatchService;

@RequestMapping("/matches")
@RestController
@RequiredArgsConstructor
public class MatchController {
	private final MatchService matchService;

	@PostMapping("/users/register")
	public CommonResponse<Void> registerUserMatch(
		@RequestParam Long prefId) {
		matchService.registerUserMatch(prefId);
		return CommonResponse.ok();
	}

	@PostMapping("/parties/register")
	public CommonResponse<Void> registerPartyMatch(
		@RequestBody PartyMatchCondCreateRequestDto requestDto) {
		matchService.registerPartyMatch(requestDto);
		return CommonResponse.ok();
	}
}

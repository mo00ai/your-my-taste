package com.example.taste.domain.match.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.match.service.MatchService;

@RequestMapping("/matches")
@RestController
@RequiredArgsConstructor
public class MatchController {
	private final MatchService matchService;

	@PostMapping
	public CommonResponse<Void> registerUserMatch(
		@RequestParam Long prefId) {
		matchService.registerUserMatch(prefId);
		return CommonResponse.ok();
	}
}

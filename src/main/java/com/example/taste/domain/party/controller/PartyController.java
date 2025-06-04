package com.example.taste.domain.party.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.service.PartyService;

@RestController
@RequestMapping("/parties")
@RequiredArgsConstructor
public class PartyController {
	private final PartyService partyService;

	@PostMapping
	public CommonResponse<Void> createParty(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody @Valid PartyCreateRequestDto requestDto) {
		partyService.createParty(userDetails.getId(), requestDto);
		return CommonResponse.ok();
	}
}

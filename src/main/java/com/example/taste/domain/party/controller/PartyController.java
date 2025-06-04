package com.example.taste.domain.party.controller;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.dto.reponse.PartyResponseDto;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.enums.PartyFilter;
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

	@GetMapping
	public CommonResponse<List<PartyResponseDto>> getParties(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(name = "filter", defaultValue = "ALL")
		@ValidEnum(target = PartyFilter.class) String partyFilter) {
		return CommonResponse.ok(partyService.getParties(userDetails.getId(), partyFilter));
	}
}

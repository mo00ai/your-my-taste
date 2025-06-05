package com.example.taste.domain.party.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
import com.example.taste.domain.party.service.PartyInvitationService;

@RestController
@RequiredArgsConstructor
public class PartyInvitationController {
	private final PartyInvitationService partyInvitationService;

	@PostMapping("/parties/{partyId}/invite")
	public CommonResponse<Void> inviteUserToParty(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @RequestBody PartyInvitationRequestDto requestDto) {
		partyInvitationService.inviteUserToParty(userDetails.getId(), partyId, requestDto);
		return CommonResponse.ok();
	}

	@PostMapping("/parties/{partyId}/join")
	public CommonResponse<Void> joinIntoParty(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId) {
		partyInvitationService.joinIntoParty(userDetails.getId(), partyId);
		return CommonResponse.ok();
	}
}

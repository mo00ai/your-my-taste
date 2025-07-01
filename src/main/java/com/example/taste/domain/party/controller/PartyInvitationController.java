package com.example.taste.domain.party.controller;

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
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
import com.example.taste.domain.party.dto.response.PartyInvitationResponseDto;
import com.example.taste.domain.party.facade.PartyInvitationFacade;
import com.example.taste.domain.party.service.PartyInvitationService;
import com.example.taste.domain.user.entity.CustomUserDetails;

@RestController
@RequestMapping("/parties/{partyId}/invitations")
@RequiredArgsConstructor
public class PartyInvitationController {
	private final PartyInvitationFacade partyInvitationFacade;
	private final PartyInvitationService partyInvitationService;

	@PostMapping("/invite")
	public CommonResponse<Void> inviteUserToParty(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @RequestBody PartyInvitationRequestDto requestDto) {
		partyInvitationService.inviteUserToParty(userDetails.getId(), partyId, requestDto);
		return CommonResponse.ok();
	}

	@PostMapping("/join")
	public CommonResponse<Void> joinIntoParty(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId) {
		partyInvitationService.joinIntoParty(userDetails.getId(), partyId);
		return CommonResponse.ok();
	}

	@GetMapping()
	public CommonResponse<List<PartyInvitationResponseDto>> getPartyInvitations(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId) {
		return CommonResponse.ok(
			partyInvitationService.getPartyInvitations(userDetails.getId(), partyId));
	}

	@PatchMapping("/{partyInvitationId}")
	public CommonResponse<Void> confirmPartyInvitation(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @PathVariable Long partyInvitationId) {
		partyInvitationFacade.confirmPartyInvitation(
			userDetails.getId(), partyId, partyInvitationId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{partyInvitationId}/cancel")
	public CommonResponse<Void> cancelPartyInvitation(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @PathVariable Long partyInvitationId) {
		partyInvitationFacade.cancelPartyInvitation(
			userDetails.getId(), partyId, partyInvitationId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{partyInvitationId}/reject")
	public CommonResponse<Void> rejectPartyInvitation(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @PathVariable Long partyInvitationId) {
		partyInvitationFacade.rejectPartyInvitation(
			userDetails.getId(), partyId, partyInvitationId);
		return CommonResponse.ok();
	}
}

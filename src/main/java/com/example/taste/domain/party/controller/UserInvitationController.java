package com.example.taste.domain.party.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.party.dto.response.UserInvitationResponseDto;
import com.example.taste.domain.party.facade.UserInvitationFacade;
import com.example.taste.domain.party.service.PartyInvitationService;
import com.example.taste.domain.user.entity.CustomUserDetails;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class UserInvitationController {
	private final PartyInvitationService partyInvitationService;
	private final UserInvitationFacade userInvitationFacade;

	@GetMapping()
	public CommonResponse<List<UserInvitationResponseDto>> getMyInvitations(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(partyInvitationService.getMyInvitations(userDetails.getId()));
	}

	@PatchMapping("/{partyInvitationId}")
	public CommonResponse<Void> confirmPartyInvitation(@PathVariable Long partyInvitationId) {
		userInvitationFacade.confirmPartyInvitation(partyInvitationId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{partyInvitationId}/cancel")
	public CommonResponse<Void> cancelPartyInvitation(@PathVariable Long partyInvitationId) {
		userInvitationFacade.cancelPartyInvitation(partyInvitationId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/{partyInvitationId}/reject")
	public CommonResponse<Void> rejectPartyInvitation(
		@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long partyInvitationId) {
		userInvitationFacade.rejectPartyInvitation(userDetails.getId(), partyInvitationId);
		return CommonResponse.ok();
	}
}

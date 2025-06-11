package com.example.taste.domain.party.controller;

import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.dto.request.InvitationActionRequestDto;
import com.example.taste.domain.party.dto.request.PartyInvitationRequestDto;
import com.example.taste.domain.party.dto.response.PartyInvitationResponseDto;
import com.example.taste.domain.party.dto.response.UserInvitationResponseDto;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
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

	@GetMapping("/users/invitations")
	public CommonResponse<List<UserInvitationResponseDto>> getMyInvitations(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(partyInvitationService.getMyInvitations(userDetails.getId()));
	}

	@GetMapping("/parties/{partyId}/invitations")
	public CommonResponse<List<PartyInvitationResponseDto>> getPartyInvitations(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId) {
		return CommonResponse.ok(
			partyInvitationService.getPartyInvitations(userDetails.getId(), partyId));
	}

	@PatchMapping("/parties/{partyId}/invitations/{partyInvitationId}")
	public CommonResponse<Void> handlePartyInvitation(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @PathVariable Long partyInvitationId,
		@RequestBody @Valid InvitationActionRequestDto requestDto) {
		// 랜덤 매칭
		if (InvitationType.valueOf(requestDto.getInvitationType()).equals(InvitationType.RANDOM)) {
			switch (InvitationStatus.valueOf(requestDto.getInvitationStatus())) {
				case CONFIRMED:
					partyInvitationService.confirmRandomPartyInvitation(
						userDetails.getId(), partyId, partyInvitationId);
					break;
				case CANCELED, REJECTED:
					partyInvitationService.rejectRandomPartyInvitation(
						userDetails.getId(), partyId, partyInvitationId, requestDto);
					break;
				default:
					throw new CustomException(INVALID_PARTY_INVITATION);
			}
		}
		// 파티 초대, 유저 가입
		else {
			switch (InvitationStatus.valueOf(requestDto.getInvitationStatus())) {
				case CONFIRMED:
					partyInvitationService.confirmManualPartyInvitation(
						userDetails.getId(), partyId, partyInvitationId);
					break;
				case CANCELED, REJECTED:
					partyInvitationService.rejectManualPartyInvitation(
						userDetails.getId(), partyId, partyInvitationId, requestDto);
					break;
				default:
					throw new CustomException(INVALID_PARTY_INVITATION);
			}
		}

		return CommonResponse.ok();
	}

	@PatchMapping("/users/invitations/{partyInvitationId}")
	public CommonResponse<Void> handleUserPartyInvitation(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyInvitationId,
		@RequestBody @Valid InvitationActionRequestDto requestDto) {
		// 랜덤 매칭
		if (InvitationType.valueOf(requestDto.getInvitationType()).equals(InvitationType.RANDOM)) {
			switch (InvitationStatus.valueOf(requestDto.getInvitationStatus())) {
				case CONFIRMED:
					partyInvitationService.confirmUserRandomPartyInvitation(
						userDetails.getId(), partyInvitationId);
					break;
				case CANCELED, REJECTED:
					partyInvitationService.rejectUserRandomPartyInvitation(
						userDetails.getId(), partyInvitationId, requestDto);
					break;
				default:
					throw new CustomException(INVALID_PARTY_INVITATION);
			}
		}
		// 파티 초대, 유저 가입
		else {
			switch (InvitationStatus.valueOf(requestDto.getInvitationStatus())) {
				case CONFIRMED:
					partyInvitationService.confirmUserManualPartyInvitation(
						userDetails.getId(), partyInvitationId);
					break;
				case CANCELED, REJECTED:
					partyInvitationService.rejectUserManualPartyInvitation(
						userDetails.getId(), partyInvitationId, requestDto);
					break;
				default:
					throw new CustomException(INVALID_PARTY_INVITATION);
			}
		}

		return CommonResponse.ok();
	}
}

package com.example.taste.domain.party.controller;

import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.party.dto.request.InvitationActionRequestDto;
import com.example.taste.domain.party.dto.response.UserInvitationResponseDto;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.service.PartyInvitationService;
import com.example.taste.domain.user.entity.CustomUserDetails;

@RestController
@RequestMapping("/invitations")
@RequiredArgsConstructor
public class UserInvitationController {
	private final PartyInvitationService partyInvitationService;

	@GetMapping()
	public CommonResponse<List<UserInvitationResponseDto>> getMyInvitations(
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(partyInvitationService.getMyInvitations(userDetails.getId()));
	}

	@PatchMapping("/{partyInvitationId}")
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
				case REJECTED:
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
				case REJECTED:
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

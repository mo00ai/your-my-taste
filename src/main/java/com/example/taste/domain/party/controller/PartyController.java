package com.example.taste.domain.party.controller;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.party.dto.request.PartyCreateRequestDto;
import com.example.taste.domain.party.dto.request.PartyUpdateRequestDto;
import com.example.taste.domain.party.dto.response.PartyDetailResponseDto;
import com.example.taste.domain.party.dto.response.PartyResponseDto;
import com.example.taste.domain.party.enums.PartyFilter;
import com.example.taste.domain.party.service.PartyInvitationService;
import com.example.taste.domain.party.service.PartyService;

@RestController
@RequestMapping("/parties")
@RequiredArgsConstructor
public class PartyController {
	private final PartyService partyService;
	private final PartyInvitationService partyInvitationService;

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
		@RequestParam(defaultValue = "ALL") @ValidEnum(target = PartyFilter.class) String filter) {
		return CommonResponse.ok(partyService.getParties(userDetails.getId(), filter));
	}

	@GetMapping("/{partyId}")
	public CommonResponse<PartyDetailResponseDto> getPartyDetail(
		@PathVariable Long partyId) {
		return CommonResponse.ok(partyService.getPartyDetail(partyId));
	}

	@PatchMapping("/{partyId}")
	public CommonResponse<Void> updatePartyDetail(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long partyId, @RequestBody @Valid PartyUpdateRequestDto requestDto) {
		partyService.updatePartyDetail(userDetails.getId(), partyId, requestDto);
		return CommonResponse.ok();
	}

	// 파티 나가기 (호스트 포함)
	@DeleteMapping("/{partyId}/leave")
	public CommonResponse<Void> leaveParty(
		@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long partyId) {
		partyInvitationService.leaveParty(userDetails.getId(), partyId);
		return CommonResponse.ok();
	}

	// 파티원 강퇴
	@DeleteMapping("/{partyId}/members/{userId}")
	public CommonResponse<Void> removePartyMember(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long userId, @PathVariable Long partyId) {
		partyInvitationService.removePartyMember(userDetails.getId(), userId, partyId);
		return CommonResponse.ok();
	}

	// 파티 삭제
	// @DeleteMapping("/{partyId}")
	// public CommonResponse<Void> removeParty(
	// 	@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long partyId) {
	// 	partyService.removeParty(userDetails.getId(), partyId);
	// 	return CommonResponse.ok();
	// }
}

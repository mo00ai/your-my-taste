package com.example.taste.domain.party.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;

@Getter
public class PartyInvitationResponseDto {
	private Long invitationId;
	private String invitationType;
	private String invitationStatus;
	private Long partyId;
	private UserSimpleResponseDto user;

	@Builder
	public PartyInvitationResponseDto(PartyInvitation partyInvitation) {
		this.invitationId = partyInvitation.getId();
		this.partyId = partyInvitation.getParty().getId();
		this.user = new UserSimpleResponseDto(partyInvitation.getUser());
		this.invitationType = partyInvitation.getInvitationType().toString();
		this.invitationStatus = partyInvitation.getInvitationStatus().toString();
	}
}

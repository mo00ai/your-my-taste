package com.example.taste.domain.party.dto.reponse;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;

@Getter
public class PartyInvitationResponseDto {
	private Long invitationId;
	private Long partyId;
	private UserSimpleResponseDto user;
	private String status;

	@Builder
	public PartyInvitationResponseDto(PartyInvitation partyInvitation) {
		this.invitationId = partyInvitation.getId();
		this.partyId = partyInvitation.getParty().getId();
		this.user = new UserSimpleResponseDto(partyInvitation.getUser());
		this.status = partyInvitation.getInvitationStatus().toString();
	}
}

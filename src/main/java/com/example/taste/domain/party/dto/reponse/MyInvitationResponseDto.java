package com.example.taste.domain.party.dto.reponse;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;

@Getter
public class MyInvitationResponseDto {
	private Long invitationId;
	private String invitationStatus;
	private PartySimpleResponseDto party;
	private UserSimpleResponseDto host;

	@Builder
	public MyInvitationResponseDto(PartyInvitation partyInvitation) {
		this.invitationId = partyInvitation.getId();
		this.invitationStatus = partyInvitation.getInvitationStatus().toString();
		this.party = new PartySimpleResponseDto(partyInvitation.getParty());
	}
}

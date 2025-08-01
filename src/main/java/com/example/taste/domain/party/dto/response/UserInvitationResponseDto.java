package com.example.taste.domain.party.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.user.dto.response.UserSimpleResponseDto;

@Getter
public class UserInvitationResponseDto {
	private Long invitationId;
	private String invitationType;
	private String invitationStatus;
	private PartySimpleResponseDto party;
	private UserSimpleResponseDto host;

	@Builder
	public UserInvitationResponseDto(PartyInvitation partyInvitation) {
		this.invitationId = partyInvitation.getId();
		this.invitationType = partyInvitation.getInvitationType().toString();
		this.invitationStatus = partyInvitation.getInvitationStatus().toString();
		this.party = party != null ? new PartySimpleResponseDto(partyInvitation.getParty()) : null;
		this.host = new UserSimpleResponseDto(partyInvitation.getParty().getHostUser());
	}
}

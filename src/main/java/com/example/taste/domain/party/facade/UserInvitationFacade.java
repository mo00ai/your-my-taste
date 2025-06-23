package com.example.taste.domain.party.facade;

import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.service.UserInvitationInternalService;

@Service
@RequiredArgsConstructor
public class UserInvitationFacade {
	private final PartyInvitationRepository partyInvitationRepository;
	private final UserInvitationInternalService internalService;

	public void confirmPartyInvitation(Long userId, Long partyInvitationId) {
		PartyInvitation partyInvitation = partyInvitationRepository.findById(partyInvitationId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));

		switch (partyInvitation.getInvitationType()) {
			case RANDOM -> internalService.confirmRandomPartyInvitation(userId, partyInvitation);
			case INVITATION -> internalService.confirmInvitedPartyInvitation(userId, partyInvitation);
			case REQUEST -> throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
			default -> throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	public void cancelPartyInvitation(Long userId, Long partyInvitationId) {
		PartyInvitation partyInvitation = partyInvitationRepository.findById(partyInvitationId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));

		if (partyInvitation.getInvitationType() == InvitationType.REQUEST) {
			internalService.cancelRequestedPartyInvitation(userId, partyInvitation);
		} else {
			throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	public void rejectPartyInvitation(Long userId, Long partyInvitationId) {
		PartyInvitation partyInvitation = partyInvitationRepository.findById(partyInvitationId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));

		switch (partyInvitation.getInvitationType()) {
			case RANDOM -> internalService.rejectRandomPartyInvitation(userId, partyInvitation);
			case INVITATION -> internalService.rejectInvitedPartyInvitation(userId, partyInvitation);
			default -> throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}
}

package com.example.taste.domain.party.facade;

import static com.example.taste.domain.party.exception.PartyErrorCode.ALREADY_EXISTS_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.INVALID_PARTY_INVITATION;
import static com.example.taste.domain.party.exception.PartyErrorCode.PARTY_INVITATION_NOT_FOUND;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.repository.PartyInvitationRepository;
import com.example.taste.domain.party.service.PartyInvitationInternalService;

@Service
@RequiredArgsConstructor
public class PartyInvitationFacade {
	private final PartyInvitationInternalService internalService;
	private final PartyInvitationRepository partyInvitationRepository;

	public void confirmPartyInvitation(Long hostId, Long partyId, Long partyInvitationId) {
		PartyInvitation partyInvitation = partyInvitationRepository.findById(partyInvitationId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));

		switch (partyInvitation.getInvitationType()) {
			case RANDOM -> internalService.confirmRandomPartyInvitation(hostId, partyId, partyInvitation);
			case REQUEST -> internalService.confirmRequestedPartyInvitation(hostId, partyId, partyInvitation);
			case INVITATION -> throw new CustomException(ALREADY_EXISTS_PARTY_INVITATION);
			default -> throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	public void cancelPartyInvitation(Long hostId, Long partyId, Long partyInvitationId) {
		PartyInvitation partyInvitation = partyInvitationRepository.findById(partyInvitationId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));

		switch (partyInvitation.getInvitationType()) {
			case RANDOM -> internalService.rejectRandomPartyInvitation(hostId, partyId, partyInvitation);
			case INVITATION -> internalService.cancelInvitedPartyInvitation(hostId, partyId, partyInvitation);
			default -> throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}

	public void rejectPartyInvitation(Long hostId, Long partyId, Long partyInvitationId) {
		PartyInvitation partyInvitation = partyInvitationRepository.findById(partyInvitationId)
			.orElseThrow(() -> new CustomException(PARTY_INVITATION_NOT_FOUND));

		switch (partyInvitation.getInvitationType()) {
			case REQUEST -> internalService.rejectRequestedPartyInvitation(hostId, partyId, partyInvitation);
			case RANDOM -> internalService.rejectRandomPartyInvitation(hostId, partyId, partyInvitation);
			default -> throw new CustomException(INVALID_PARTY_INVITATION);
		}
	}
}

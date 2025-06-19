package com.example.taste.domain.party.repository;

import java.util.List;
import java.util.Optional;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.user.entity.User;

public interface PartyInvitationRepositoryCustom {
	List<User> findUsersInParty(Long partyId);

	Optional<PartyInvitation> findByUserAndParty(Long userId, Long partyId);

	List<PartyInvitation> findAvailablePartyInvitationList(
		Long userId, InvitationStatus invitationStatus);

	List<PartyInvitation> findByPartyAndInvitationStatus(Long partyId, InvitationStatus invitationStatus);

	List<Long> findAllPartyIdByUser(Long userId);

	long deleteUserMatchByTypeAndStatus(
		Long userMatchInfoId, InvitationType type, InvitationStatus status);

	long deleteAllByPartyAndInvitationStatus(Long partyId, InvitationStatus status);

	List<PartyInvitation> findByPartyAndInvitationTypeAndStatus(Long id, InvitationType invitationType,
		InvitationStatus invitationStatus);

	boolean isConfirmedPartyMember(Long partyId, Long userId);
}

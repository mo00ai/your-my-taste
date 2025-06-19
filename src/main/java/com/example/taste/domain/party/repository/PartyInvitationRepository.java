package com.example.taste.domain.party.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;

@Repository
public interface PartyInvitationRepository extends
	JpaRepository<PartyInvitation, Long>, PartyInvitationRepositoryCustom {
	List<PartyInvitation> findByPartyId(Long partyId);

	boolean existsByUserIdAndPartyIdAndInvitationStatus(
		Long userId, Long partyId, InvitationStatus invitationStatus);
}

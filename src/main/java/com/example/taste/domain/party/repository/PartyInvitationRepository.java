package com.example.taste.domain.party.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.user.entity.User;

@Repository
public interface PartyInvitationRepository extends JpaRepository<PartyInvitation, Long> {
	@Query("SELECT DISTINCT pi.user FROM PartyInvitation pi "
		+ "WHERE pi.party.id = :partyId AND pi.invitationStatus = 'CONFIRMED'")
	List<User> findUsersInParty(@Param("partyId") Long partyId);

	@Query("SELECT pi FROM PartyInvitation pi "
		+ "WHERE pi.user.id = :userId AND pi.party.id = :partyId")
	Optional<PartyInvitation> findByUserAndParty(
		@Param("userId") Long userId, @Param("partyId") Long partyId);

	List<PartyInvitation> findByUserIdAndInvitationStatus(Long userId, InvitationStatus invitationStatus);

	List<PartyInvitation> findByPartyIdAndInvitationStatus(Long partyId, InvitationStatus invitationStatus);
}

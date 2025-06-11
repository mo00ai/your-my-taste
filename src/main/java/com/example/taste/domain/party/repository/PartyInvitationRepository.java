package com.example.taste.domain.party.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.PartyStatus;
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

	@Query("SELECT pi FROM PartyInvitation pi "
		+ "WHERE pi.user.id = :userId AND pi.invitationStatus = :invitationStatus "
		+ "AND pi.party.partyStatus = :partyStatus")
	List<PartyInvitation> findMyActivePartyInvitationList(
		@Param("userId") Long userId, @Param("invitationStatus") InvitationStatus invitationStatus,
		@Param("partyStatus") PartyStatus partyStatus);

	List<PartyInvitation> findByPartyIdAndInvitationStatus(Long partyId, InvitationStatus invitationStatus);

	@Query("SELECT pi.party.id FROM PartyInvitation pi WHERE pi.user.id = :userId")
	List<Long> findAllPartyIdByUser(@Param("user") User user);

	@Query("DELETE FROM PartyInvitation pi "
		+ "WHERE pi.userMatchCond = :userMatchCond "
		+ "AND pi.invitationType = :type AND pi.invitationStatus = :status")
	void deleteUserMatchWhileMatching(
		@Param("userMatchCond") UserMatchCond userMatchCond,
		@Param("invitationType") InvitationType type,
		@Param("invitationStatus") InvitationStatus status);

	@Query("DELETE FROM PartyInvitation pi "
		+ "WHERE pi.party = :party "
		+ "AND pi.invitationType = :type AND pi.invitationStatus = :status")
	void deletePartyMatchWhileMatching(@Param("party") Party party,
		@Param("type") InvitationType type, @Param("status") InvitationStatus status);
}

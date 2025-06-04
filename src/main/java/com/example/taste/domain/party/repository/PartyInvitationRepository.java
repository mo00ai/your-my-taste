package com.example.taste.domain.party.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.user.entity.User;

@Repository
public interface PartyInvitationRepository extends JpaRepository<PartyInvitation, Long> {
	@Query("SELECT DISTINCT pi.user FROM PartyInvitation pi "
		+ "WHERE pi.party.id = :partyId AND pi.invitationStatus = 'CONFIRMED'")
	List<User> findUsersInParty(@Param("partyId") Long partyId);
}

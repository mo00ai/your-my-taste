package com.example.taste.domain.party.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.entity.Party;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
	@Query("SELECT DISTINCT p FROM Party p JOIN FETCH p.hostUser LEFT JOIN FETCH p.store "
		+ "WHERE p.hostUser.id != :hostId AND p.partyStatus = 'RECRUITING'")
	List<Party> findAllByRecruitingAndUserNotIn(@Param("hostId") Long hostId);

	@Query(
		"SELECT DISTINCT pi.party FROM PartyInvitation pi "
			+ "JOIN FETCH pi.party.hostUser LEFT JOIN FETCH pi.party.store "
			+ "WHERE pi.user.id = :userId AND pi.party.partyStatus = 'RECRUITING'")
	List<Party> findAllByRecruitingUserIn(@Param("userId") Long userId);

	@Query("SELECT p FROM Party p LEFT JOIN FETCH p.partyInvitationList pi "
		+ "LEFT JOIN FETCH pi.user WHERE p.id = :partyId")
	Optional<Party> findByIdWithInvitationsAndUsers(@Param("partyId") Long partyId);
}

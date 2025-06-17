package com.example.taste.domain.party.repository;

import java.util.List;
import java.util.Optional;

import com.example.taste.domain.party.entity.Party;

public interface PartyRepositoryCustom {
	List<Party> findAllByRecruitingAndUserNotIn(Long hostId);

	List<Party> findAllByUserIn(Long userId);

	Optional<Party> findByIdWithInvitationsAndUsers(Long partyId);
}

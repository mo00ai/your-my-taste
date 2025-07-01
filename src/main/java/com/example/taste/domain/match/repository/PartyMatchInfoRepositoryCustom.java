package com.example.taste.domain.match.repository;

import java.util.Optional;

public interface PartyMatchInfoRepositoryCustom {
	Optional<Long> findIdByPartyId(Long partyId);
}

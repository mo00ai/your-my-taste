package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.PartyMatchCond;
import com.example.taste.domain.party.entity.Party;

public interface PartyMatchCondRepository extends JpaRepository<PartyMatchCond, Long> {
	boolean existsPartyMatchCondByParty(Party Party);

	void deleteByParty(Party party);

	PartyMatchCond findPartyMatchCondByParty(Party party);
}

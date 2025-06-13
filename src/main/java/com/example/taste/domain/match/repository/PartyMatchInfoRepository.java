package com.example.taste.domain.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.party.entity.Party;

public interface PartyMatchInfoRepository extends JpaRepository<PartyMatchInfo, Long> {
	boolean existsPartyMatchInfoByParty(Party Party);

	void deleteByParty(Party party);

	PartyMatchInfo findPartyMatchInfoByParty(Party party);
}

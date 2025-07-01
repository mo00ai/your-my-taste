package com.example.taste.domain.match.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taste.domain.match.entity.PartyMatchInfo;
import com.example.taste.domain.party.entity.Party;

public interface PartyMatchInfoRepository extends JpaRepository<PartyMatchInfo, Long>, PartyMatchInfoRepositoryCustom {
	boolean existsPartyMatchInfoByParty(Party Party);

	void deleteByParty(Party party);

	Optional<PartyMatchInfo> findPartyMatchInfoByParty(Party party);
}

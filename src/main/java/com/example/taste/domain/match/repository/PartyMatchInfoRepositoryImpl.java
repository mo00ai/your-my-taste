package com.example.taste.domain.match.repository;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import com.example.taste.domain.match.entity.QPartyMatchInfo;
import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
@RequiredArgsConstructor
public class PartyMatchInfoRepositoryImpl implements PartyMatchInfoRepositoryCustom {
	private final JPAQueryFactory queryFactory;
	private final QPartyMatchInfo pmi = QPartyMatchInfo.partyMatchInfo;

	@Override
	public Optional<Long> findIdByPartyId(Long partyId) {
		return Optional.ofNullable(queryFactory
			.select(pmi.id)
			.from(pmi)
			.where(pmi.party.id.eq(partyId))
			.fetchOne());
	}
}

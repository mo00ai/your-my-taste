package com.example.taste.domain.party.repository;

import java.util.List;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.QParty;
import com.example.taste.domain.party.entity.QPartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.store.entity.QStore;
import com.example.taste.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class PartyRepositoryImpl implements PartyRepositoryCustom {
	private final JPAQueryFactory queryFactory;
	private final QUser user = QUser.user;
	private final QPartyInvitation pi = QPartyInvitation.partyInvitation;
	private final QParty p = QParty.party;
	private final QStore store = QStore.store;

	public PartyRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
		this.queryFactory = jpaQueryFactory;
	}

	@Override
	public List<Party> findAllByRecruitingAndUserNotIn(Long userId) {
		return queryFactory
			.select(p)
			.from(p)
			.join(p.hostUser, user).fetchJoin()
			.leftJoin(p.store).fetchJoin()
			.leftJoin(pi).on(
				pi.party.eq(p),
				pi.invitationStatus.eq(InvitationStatus.CONFIRMED)
			)
			.where(
				p.partyStatus.eq(PartyStatus.ACTIVE),
				p.hostUser.id.ne(userId)
			)
			.groupBy(p.id)
			.having(pi.count().lt(p.maxMembers))
			.fetch();
	}

	@Override
	public List<Party> findAllByUserIn(Long userId) {
		return queryFactory
			.selectDistinct(p)
			.from(pi)
			.join(pi.party, p)
			.join(p.hostUser, user).fetchJoin()
			.leftJoin(p.store).fetchJoin()
			.where(pi.user.id.eq(userId))
			.fetch();
	}
}

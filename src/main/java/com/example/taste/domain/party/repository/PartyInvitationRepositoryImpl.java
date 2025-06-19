package com.example.taste.domain.party.repository;

import java.util.List;
import java.util.Optional;

import com.example.taste.domain.party.entity.PartyInvitation;
import com.example.taste.domain.party.entity.QParty;
import com.example.taste.domain.party.entity.QPartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.InvitationType;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.user.entity.User;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

public class PartyInvitationRepositoryImpl implements PartyInvitationRepositoryCustom {
	private final JPAQueryFactory queryFactory;
	private final QPartyInvitation pi = QPartyInvitation.partyInvitation;
	private final QPartyInvitation piSub = new QPartyInvitation("piSub");
	private final QParty p = QParty.party;

	public PartyInvitationRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
		this.queryFactory = jpaQueryFactory;
	}

	@Override
	public List<User> findUsersInParty(Long partyId) {
		return queryFactory
			.select(pi.user)
			.from(pi)
			.where(pi.party.id.eq(partyId),
				pi.invitationStatus.eq(InvitationStatus.CONFIRMED))
			.fetch();
	}

	@Override
	public Optional<PartyInvitation> findByUserAndParty(Long userId, Long partyId) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(pi)
				.where(
					pi.user.id.eq(userId),
					pi.party.id.eq(partyId))
				.fetchOne()
		);
	}

	@Override
	public List<PartyInvitation> findAvailablePartyInvitationList(Long userId,
		InvitationStatus invitationStatus) {
		return queryFactory
			.selectFrom(pi)
			.join(pi.party, p).fetchJoin()
			.where(
				pi.user.id.eq(userId),
				pi.invitationStatus.eq(invitationStatus),
				p.partyStatus.eq(PartyStatus.ACTIVE),
				JPAExpressions
					.select(piSub.count().intValue())
					.from(piSub)
					.where(
						piSub.party.id.eq(p.id),
						piSub.invitationStatus.eq(InvitationStatus.CONFIRMED)
					)
					.lt(p.maxMembers.castToNum(Integer.class))
			)
			.fetch();
	}

	@Override
	public List<PartyInvitation> findByPartyAndInvitationStatus(Long partyId, InvitationStatus invitationStatus) {
		return queryFactory
			.selectFrom(pi)
			.where(
				pi.party.id.eq(partyId),
				pi.invitationStatus.eq(invitationStatus)
			)
			.fetch();
	}

	@Override
	public List<Long> findAllPartyIdByUser(Long userId) {
		return queryFactory
			.select(pi.party.id)
			.from(pi)
			.where(pi.user.id.eq(userId))
			.fetch();
	}

	@Override
	public long deleteUserMatchByTypeAndStatus(Long userMatchInfoId, InvitationType type,
		InvitationStatus status) {
		return queryFactory
			.delete(pi)
			.where(
				pi.userMatchInfo.id.eq(userMatchInfoId),
				pi.invitationType.eq(type),
				pi.invitationStatus.eq(status)
			)
			.execute();
	}

	@Override
	public long deleteAllByPartyAndInvitationStatus(Long partyId, InvitationStatus status) {
		return queryFactory
			.delete(pi)
			.where(
				pi.party.id.eq(partyId),
				pi.invitationStatus.eq(status)
			)
			.execute();
	}

	@Override
	public List<PartyInvitation> findByPartyAndInvitationTypeAndStatus(Long partyId,
		InvitationType invitationType, InvitationStatus invitationStatus) {
		return queryFactory
			.selectFrom(pi)
			.join(pi.party, p).fetchJoin()
			.where(p.id.eq(partyId),
				pi.invitationType.eq(invitationType),
				pi.invitationStatus.eq(invitationStatus))
			.fetch();
	}

	@Override
	public boolean isConfirmedPartyMember(Long partyId, Long userId) {
		return queryFactory
			.selectOne()
			.from(pi)
			.where(
				pi.party.id.eq(partyId),
				pi.user.id.eq(userId),
				pi.invitationStatus.eq(InvitationStatus.CONFIRMED)
			)
			.fetchFirst() != null;
	}
}

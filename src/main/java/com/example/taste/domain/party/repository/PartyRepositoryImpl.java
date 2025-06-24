package com.example.taste.domain.party.repository;

import static com.example.taste.domain.party.enums.PartySort.CREATED_AT;
import static com.example.taste.domain.party.enums.PartySort.MEETING_DATE;
import static com.example.taste.domain.party.enums.PartySort.NEARLY_FULL;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;

import com.example.taste.domain.party.entity.Party;
import com.example.taste.domain.party.entity.QParty;
import com.example.taste.domain.party.entity.QPartyInvitation;
import com.example.taste.domain.party.enums.InvitationStatus;
import com.example.taste.domain.party.enums.PartyStatus;
import com.example.taste.domain.store.entity.QStore;
import com.example.taste.domain.user.entity.QUser;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.PathBuilder;
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
	public Slice<Party> findAllByActiveAndUserNotInSorted(
		Long userId, Pageable pageable) {
		List<Party> results = queryFactory
			.select(p)
			.from(p)
			.join(p.hostUser, user).fetchJoin()
			.leftJoin(p.store).fetchJoin()
			.leftJoin(pi).on(
				pi.party.eq(p),
				pi.invitationStatus.eq(InvitationStatus.CONFIRMED))
			.where(
				p.partyStatus.eq(PartyStatus.ACTIVE),
				p.hostUser.id.ne(userId))
			.groupBy(p.id)
			.having(pi.count().lt(p.maxMembers))
			.orderBy(getOrderSpecifier(pageable))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = results.size() > pageable.getPageSize();
		if (results.size() > pageable.getPageSize()) {
			results.remove(results.size() - 1);
		}

		return hasNext(results, pageable);
	}

	@Override
	public Slice<Party> findAllByUserInSorted(
		Long userId, Pageable pageable) {
		List<Party> results = queryFactory
			.selectDistinct(p)
			.from(pi)
			.join(pi.party, p)
			.join(p.hostUser, user).fetchJoin()
			.leftJoin(p.store).fetchJoin()
			.where(pi.user.id.eq(userId))
			.fetch();

		return hasNext(results, pageable);
	}

	@Override
	public List<Party> findAllMeetingDateAndDeleteAtIsNull(LocalDate meetingDate) {
		return queryFactory
			.selectFrom(p)
			.where(
				p.meetingDate.loe(meetingDate),
				p.deletedAt.isNull()
			).fetch();
	}

	private OrderSpecifier<?> getOrderSpecifier(Pageable pageable) {

		if (pageable.getSort().isEmpty()) {
			return p.meetingDate.asc();
		}

		for (Sort.Order order : pageable.getSort()) {
			PathBuilder<Party> path = new PathBuilder<>(Party.class, "party");

			if (order.getProperty().equals(MEETING_DATE.getLabel())) {
				return order.isDescending() ?
					path.getDateTime(MEETING_DATE.getLabel(), LocalDate.class).desc()
					: path.getDateTime(MEETING_DATE.getLabel(), LocalDate.class).asc();
			}
			if (order.getProperty().equals(CREATED_AT.getLabel())) {
				return order.isDescending() ? path.getString(CREATED_AT.getLabel()).desc()
					: path.getString(CREATED_AT.getLabel()).asc();
			}
			if (order.getProperty().equals(NEARLY_FULL.getLabel())) {
				NumberExpression<Integer> remainingCount = path.getNumber("maxMembers", Integer.class)
					.subtract(path.getNumber("nowMembers", Integer.class));

				return order.isAscending() ? remainingCount.asc() : remainingCount.desc();
			}
		}

		return p.meetingDate.desc();
	}

	private SliceImpl<Party> hasNext(List<Party> results, Pageable pageable) {
		boolean hasNext = results.size() > pageable.getPageSize();
		if (results.size() > pageable.getPageSize()) {
			results.remove(results.size() - 1);
		}

		return new SliceImpl<>(results, pageable, hasNext);
	}
}

package com.example.taste.domain.party.repository;

import static com.example.jooq.Tables.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.party.enums.PartyStatus;

@Repository
public class PartyRepositoryJooqImpl implements PartyRepositoryJooqCustom {

	private final DSLContext dsl;

	public PartyRepositoryJooqImpl(DSLContext dsl) {
		this.dsl = dsl;
	}

	@Override
	public List<Long> findAllByMeetingDate(LocalDate meetingDate) {
		return dsl.select(PARTY.ID)
			.from(PARTY)
			.where(PARTY.MEETING_DATE.eq(meetingDate))
			.fetch()
			.map(r -> r.get(PARTY.ID));
	}

	@Override
	public long updateExpiredStateByIds(List<? extends Long> ids) {
		return dsl.update(PARTY)
			.set(PARTY.PARTY_STATUS, PartyStatus.EXPIRED.name())
			.where(PARTY.ID.in(ids))
			.execute();
	}

	@Override
	public long softDeleteByIds(List<? extends Long> ids) {
		LocalDateTime now = LocalDateTime.now();
		return dsl.update(PARTY)
			.set(PARTY.DELETED_AT, now)
			.where(PARTY.ID.in(ids))
			.execute();
	}
}


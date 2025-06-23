package com.example.taste.domain.board.repository;

import static com.example.jooq.Tables.*;

import java.sql.Timestamp;
import java.util.List;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.board.entity.AccessPolicy;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryJooqCustomImpl implements BoardRepositoryJooqCustom {

	private final DSLContext dsl;

	@Override
	public List<Long> findExpiredTimeAttackBoardIds(AccessPolicy policy) {
		return dsl.select(BOARD.ID)
			.from(BOARD)
			.where(BOARD.ACCESS_POLICY.eq(policy.name())) // Bind variable
			.and(DSL.field("DATE_ADD({0}, INTERVAL {1} MINUTE)",
					Timestamp.class,
					BOARD.OPEN_TIME,
					BOARD.OPEN_LIMIT)
				.le(DSL.currentTimestamp()))
			.fetch(BOARD.ID);
	}

	@Override
	public long closeBoardsByIds(List<? extends Long> ids) {
		return dsl.update(BOARD)
			.set(BOARD.ACCESS_POLICY, AccessPolicy.CLOSED.name())
			.where(BOARD.ID.in(ids))
			.execute();
	}
}

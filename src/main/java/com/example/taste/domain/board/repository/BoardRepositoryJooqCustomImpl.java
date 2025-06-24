package com.example.taste.domain.board.repository;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BoardRepositoryJooqCustomImpl implements BoardRepositoryJooqCustom {

	private final DSLContext dsl;

	@Override
	public List<Long> findExpiredTimeAttackBoardIds(String policy) {
		// return dsl.select(BOARD.ID)
		// 	.from(BOARD)
		// 	.where(BOARD.ACCESS_POLICY.eq(BoardAccessPolicy.valueOf(policy))) // Bind variable
		// 	.and(DSL.field("DATE_ADD({0}, INTERVAL {1} MINUTE)",
		// 			Timestamp.class,
		// 			BOARD.OPEN_TIME,
		// 			BOARD.OPEN_LIMIT)
		// 		.le(DSL.currentTimestamp()))
		// 	.fetch(BOARD.ID);
		return null;
	}
}

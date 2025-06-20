package com.example.taste.domain.user.repository;

import static com.example.jooq.Tables.*;

import org.jooq.DSLContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserRepositoryJooqCustomImpl implements UserRepositoryJooqCustom {

	private final DSLContext dsl;

	@Override
	public void resetAllUserPoints() {
		dsl.update(USERS)
			.set(USERS.POINT, 0)
			.where(USERS.POINT.ne(0))
			.execute();
	}
}

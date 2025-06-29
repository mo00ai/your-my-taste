package com.example.taste.domain.user.repository;

import org.jooq.Cursor;

import com.example.jooq.tables.records.UsersRecord;

public interface UserRepositoryJooqCustom {
	void resetAllUserPoints();

	Cursor<UsersRecord> findByPointWithJooqCursor(int point);
}

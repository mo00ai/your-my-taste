package com.example.taste.domain.user.repository;

import static com.example.jooq.Tables.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import org.jooq.DSLContext;
import org.springframework.dao.DataAccessException;

import com.example.taste.common.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserRepositoryJooqCustomImpl implements UserRepositoryJooqCustom {

	private final DSLContext dsl;

	@Override
	public void resetAllUserPoints() {
		try {
			int updatedRows = dsl.update(USERS)
				.set(USERS.POINT, 0)
				.where(USERS.POINT.ne(0))
				.execute();
			log.info(" [PkRanking] 포인트 리셋된 유저 수 : {}", updatedRows);
		} catch (DataAccessException e) {
			throw new CustomException(USER_POINT_RESET_FAILED);
		}
	}
}

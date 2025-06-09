package com.example.taste.domain.user.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	public void resetAllUserPoints() {
		String sql = "UPDATE users SET point = 0 WHERE point != 0 ";
		jdbcTemplate.update(sql);
	}
}

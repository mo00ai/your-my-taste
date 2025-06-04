package com.example.taste.domain.user.service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.taste.domain.user.entity.UserFavor;
import com.example.taste.domain.user.repository.UserFavorRepository;

@Service
@RequiredArgsConstructor
public class UserFavorService {
	private final UserFavorRepository userFavorRepository;
	private final JdbcTemplate jdbcTemplate;
	private final EntityManager em;

	// bulk insert 저장
	public void saveUserFavorList(List<UserFavor> userFavorList) {
		String INSERT_SQL = "INSERT INTO user_favor (favor_id, user_id) VALUES (?, ?)";

		for (UserFavor uf : userFavorList) {
			jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setLong(1, uf.getFavor().getId());
					ps.setLong(2, uf.getUser().getId());
				}

				@Override
				public int getBatchSize() {
					return 100;
				}
			});
		}
	}
}

package com.example.taste.domain.pk.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PkLogJdbcRepository {

	private final JdbcTemplate jdbcTemplate;

	// public void batchInsert(List<PkLog> pkLogs) {
	// 	String sql = " insert into pk_log (pk_type, point, created_at, user_id) values (?, ?, ?, ?) ";
	//
	// 	jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
	// 		@Override
	// 		public void setValues(PreparedStatement ps, int i) throws SQLException {
	// 			PkLog pkLog = pkLogs.get(i);
	// 			ps.setString(1, pkLog.getPkType().name());
	// 			ps.setInt(2, pkLog.getPoint());
	// 			ps.setTimestamp(3, Timestamp.valueOf(pkLog.getCreatedAt()));
	// 			ps.setLong(4, pkLog.getUser().getId());
	// 		}
	//
	// 		@Override
	// 		public int getBatchSize() {
	// 			return pkLogs.size();
	// 		}
	// 	});
	// }
}

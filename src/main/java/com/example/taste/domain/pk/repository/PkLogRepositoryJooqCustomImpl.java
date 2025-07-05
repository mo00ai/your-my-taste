package com.example.taste.domain.pk.repository;

import static com.example.jooq.Tables.*;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.pk.entity.PkLog;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PkLogRepositoryJooqCustomImpl implements PkLogRepositoryJooqCustom {

	private final DSLContext dsl;

	@Override
	public void insertPkLogs(List<PkLog> pkLogs) {
		final int batchSize = 10_000;

		for (int i = 0; i < pkLogs.size(); i += batchSize) {
			int end = Math.min(i + batchSize, pkLogs.size());
			List<PkLog> subList = pkLogs.subList(i, end);

			var records = subList.stream()
				.map(log -> dsl.newRecord(PK_LOG)
					.with(PK_LOG.PK_TYPE, log.getPkType().name())
					.with(PK_LOG.POINT, log.getPoint())
					.with(PK_LOG.CREATED_AT, log.getCreatedAt())
					.with(PK_LOG.USER_ID, log.getUser().getId()))
				.toList();

			dsl.batchInsert(records).execute();
		}
	}
}

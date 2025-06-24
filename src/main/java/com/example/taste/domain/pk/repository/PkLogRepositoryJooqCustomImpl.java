package com.example.taste.domain.pk.repository;

import static com.example.jooq.Tables.*;

import java.util.List;

import org.jooq.DSLContext;
import org.jooq.Query;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.pk.entity.PkLog;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PkLogRepositoryJooqCustomImpl implements PkLogRepositoryJooqCustom {

	private final DSLContext dsl;

	@Override
	public void insertPkLogs(List<PkLog> pkLogs) {

		List<Query> batchQueries = pkLogs.stream()
			.map(log -> (Query)dsl.insertInto(PK_LOG)
				.set(PK_LOG.PK_TYPE, log.getPkType().name())
				.set(PK_LOG.POINT, log.getPoint())
				.set(PK_LOG.CREATED_AT, log.getCreatedAt())
				.set(PK_LOG.USER_ID, log.getUser().getId()))
			.toList();

		dsl.batch(batchQueries).execute();
	}
}

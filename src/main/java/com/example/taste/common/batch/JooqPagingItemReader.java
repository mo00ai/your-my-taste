package com.example.taste.common.batch;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;

import org.jooq.DSLContext;
import org.jooq.SelectSeekStepN;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Qualifier("jooqDatabaseReader")
public class JooqPagingItemReader<T> extends AbstractPagingItemReader<T> {

	private final DSLContext dsl;
	private final Function<DSLContext, SelectSeekStepN<? extends Record>> queryFunction;
	private final Function<Record, T> recordMapper;

	public JooqPagingItemReader(DSLContext dsl, Function<DSLContext, SelectSeekStepN<? extends Record>> queryFunction,
		Function<Record, T> recordMapper) {
		this.dsl = dsl;
		this.queryFunction = queryFunction;
		this.recordMapper = recordMapper;
	}

	@Override
	protected void doReadPage() {
		int start = getPage() * getPageSize();

		if (results == null) {
			results = new CopyOnWriteArrayList<>();
		} else {
			results.clear();
		}

		List<T> partyList = queryFunction.apply(dsl)
			.seek(start)
			.limit(getPageSize())
			.fetch()
			.stream()
			.map(recordMapper)
			.toList();

		results.addAll(partyList);
	}
}


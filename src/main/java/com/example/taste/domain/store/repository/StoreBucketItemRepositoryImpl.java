package com.example.taste.domain.store.repository;

import static com.example.taste.domain.store.entity.QStore.*;
import static com.example.taste.domain.store.entity.QStoreBucketItem.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.store.dto.response.BucketItemResponse;
import com.example.taste.domain.store.entity.StoreBucket;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class StoreBucketItemRepositoryImpl implements StoreBucketItemRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<BucketItemResponse> findAllByStoreBucket(StoreBucket storeBucket, Pageable pageable) {
		List<BucketItemResponse> result = queryFactory
			.select(Projections.constructor(BucketItemResponse.class,
				storeBucketItem.id,
				store.id,
				store.name,
				store.mapx,
				store.mapy))
			.from(storeBucketItem)
			.join(storeBucketItem.store, store)
			.where(storeBucketItem.storeBucket.eq(storeBucket))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(storeBucketItem.count())
			.from(storeBucketItem)
			.where(storeBucketItem.storeBucket.eq(storeBucket))
			.fetchOne();

		return new PageImpl<>(result, pageable, total != null ? total : 0L);
	}
}

package com.example.taste.domain.store.repository;

import static com.example.taste.domain.store.entity.QStore.*;
import static com.example.taste.domain.store.entity.QStoreBucket.*;
import static com.example.taste.domain.store.entity.QStoreBucketItem.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.user.entity.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class StoreBucketRepositoryImpl implements StoreBucketRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<StoreBucket> searchMyBuckets(User user, String keyword, Pageable pageable) {
		List<StoreBucket> results = queryFactory
			.select(storeBucket).distinct()
			.from(storeBucket)
			.join(storeBucketItem).on(storeBucket.eq(storeBucketItem.storeBucket))
			.join(store).on(storeBucketItem.store.eq(store))
			.where(
				storeBucket.user.eq(user),
				hasKeyword(keyword)    // keyword null이면 자동으로 조건에서 무시됨
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(storeBucket.id.desc())
			.fetch();    // 리스트 반환

		Long total = queryFactory
			.select(storeBucket.count())
			.from(storeBucket)
			.where(
				storeBucket.user.eq(user),
				hasKeyword(keyword)
			)
			.fetchOne();
		return new PageImpl<>(results, pageable, total != null ? total : 0L);
	}

	@Override
	public Page<StoreBucket> searchFollowingsBucketsWithKeyword(List<Long> followingIds, String keyword,
		Pageable pageable) {
		List<StoreBucket> results = queryFactory
			.select(storeBucket).distinct()
			.from(storeBucket)
			.join(storeBucketItem).on(storeBucket.eq(storeBucketItem.storeBucket))
			.join(store).on(storeBucketItem.store.eq(store))
			.where(
				storeBucket.user.id.in(followingIds), // 팔로우한 사람들 버킷 필터링
				storeBucket.isOpened.isTrue(),
				hasKeyword(keyword)
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(storeBucket.id.desc())
			.fetch();

		Long total = queryFactory
			.select(storeBucket.countDistinct())
			.from(storeBucket)
			.join(storeBucketItem).on(storeBucket.eq(storeBucketItem.storeBucket))
			.join(store).on(storeBucketItem.store.eq(store))
			.where(
				storeBucket.user.id.in(followingIds),
				storeBucket.isOpened.isTrue(),
				hasKeyword(keyword)
			)
			.fetchOne();
		return new PageImpl<>(results, pageable, total != null ? total : 0L);
	}

	private BooleanExpression hasKeyword(String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return null;
		}
		return store.name.containsIgnoreCase(keyword)    // 가게명에 키워드 포함되어 있거나
			.or(store.description.containsIgnoreCase(keyword))    // 가게의 설명에 포함되어 있거나
			.or(store.category.name.containsIgnoreCase(keyword)); // 카테고리명 포함 여부 추가
	}
}

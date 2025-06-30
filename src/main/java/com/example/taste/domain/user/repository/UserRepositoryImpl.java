package com.example.taste.domain.user.repository;

import static com.example.taste.domain.user.entity.QUser.user;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public long resetPostingCnt() {
		return queryFactory.update(user)
			.set(user.postingCount, 0)
			.execute();
	}

	// @Override
	// public long resetPostingCnt() {
	// 	return queryFactory.update(user)
	// 		.set(user.postingCount, 0)
	// 		.where(user.postingCount.ne(0))
	// 		.execute();
	// }

	@Override
	public int increasePostingCount(Long userId, int limit) {
		return (int)queryFactory.update(user)
			.set(user.postingCount, user.postingCount.add(1))
			.where(
				user.id.eq(userId),
				user.postingCount.lt(limit)
			)
			.execute();
	}

	@Override
	public Integer findAgeByUserId(Long userId) {
		return queryFactory
			.select(user.age)
			.from(user)
			.where(user.id.eq(userId))
			.fetchOne();
	}
}

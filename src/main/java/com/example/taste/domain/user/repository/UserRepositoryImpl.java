package com.example.taste.domain.user.repository;

import static com.example.taste.domain.favor.entity.QFavor.*;
import static com.example.taste.domain.user.entity.QUser.*;
import static com.example.taste.domain.user.entity.QUserFavor.*;

import java.util.Optional;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

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
	public Optional<User> findUserWithFavors(Long userId) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(user)
				.leftJoin(user.userFavorList, userFavor).fetchJoin()
				.leftJoin(userFavor.favor, favor).fetchJoin()
				.where(user.id.eq(userId))
				.fetchOne()
		);
	}


	@Override
	public Page<Long> getAllUserIdPage(PageRequest pageRequest) {
		List<Long> userPage = queryFactory.select(user.id).from(user).where(
				user.deletedAt.isNull()
			)
			.orderBy(user.id.asc())
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.fetch();
		Long total = queryFactory.select(user.count())
			.from(user)
			.where(user.deletedAt.isNull())
			.fetchOne();
		return new PageImpl<>(userPage, pageRequest, total);
	}
}

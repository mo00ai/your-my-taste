package com.example.taste.domain.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.taste.domain.user.entity.QFollow;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepositroryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Long> findAllIdByFollowing(Long followingId, PageRequest pageRequest) {
		QFollow qFollow = QFollow.follow;
		List<Long> allFollowId = queryFactory.select(qFollow.id).from(qFollow)
			.where(
				qFollow.following.id.eq(followingId)
			)
			.orderBy(qFollow.id.asc())
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.fetch();

		Long total = queryFactory.select(qFollow.count())
			.from(qFollow)
			.where(qFollow.following.id.eq(followingId))
			.fetchOne();

		return new PageImpl<>(allFollowId, pageRequest, total);
	}
}

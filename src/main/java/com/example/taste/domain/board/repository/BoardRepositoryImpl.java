package com.example.taste.domain.board.repository;

import static com.example.taste.domain.board.entity.QBoard.*;
import static com.example.taste.domain.image.entity.QBoardImage.*;
import static com.example.taste.domain.store.entity.QStore.*;
import static com.example.taste.domain.user.entity.QUser.*;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.search.BoardSearchCondition;
import com.example.taste.domain.board.entity.AccessPolicy;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class BoardRepositoryImpl implements BoardRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<BoardListResponseDto> findBoardListDtoByUserIdList(List<Long> userIds, Pageable pageable) {
		List<BoardListResponseDto> contents = queryFactory
			.select(Projections.constructor(BoardListResponseDto.class,
				board.id,
				board.title,
				store.name,
				user.nickname,
				JPAExpressions        // 서브쿼리 게시글&이미지 테이블에서 이미지 조회 -> 없으면 null반환
					.select(boardImage.image.url)
					.from(boardImage)
					.where(boardImage.board.eq(board))
					.orderBy(boardImage.id.asc())
					.limit(1)
			))
			.from(board)
			.leftJoin(board.user, user)
			.leftJoin(board.store, store)
			//.leftJoin(board.boardImageList, boardImage)
			.where(board.user.id.in(userIds))
			//.groupBy(board.id, board.title, store.name, user.nickname)
			.orderBy(board.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(board.count())
			.from(board)
			.where(board.user.id.in(userIds))
			.fetchOne();

		return new PageImpl<>(contents, pageable, total != null ? total : 0L);
	}

	@Override
	public Page<BoardListResponseDto> searchBoardsByKeyword(BoardSearchCondition condition, Pageable pageable) {
		List<BoardListResponseDto> contents = queryFactory
			.select(Projections.constructor(BoardListResponseDto.class,
				board.id,
				board.title,
				store.name,
				user.nickname,    // 작성자 이름
				JPAExpressions        // 서브쿼리 게시글&이미지 테이블에서 이미지 조회 -> 없으면 null반환
					.select(boardImage.image.url)
					.from(boardImage)
					.where(boardImage.board.eq(board))
					.orderBy(boardImage.id.asc())
					.limit(1)
			))
			.from(board)
			.leftJoin(board.user, user)
			.leftJoin(board.store, store)
			//.leftJoin(board.boardImageList, boardImage)
			.where(buildSearchConditions(condition))
			//.groupBy(board.id, board.title, store.name, user.nickname)
			.orderBy(getOrderSpecifier(pageable).toArray(new OrderSpecifier[0]))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(board.count())
			.from(board)
			.where(buildSearchConditions(condition))  // 동일한 조건 재사용
			.fetchOne();

		return new PageImpl<>(contents, pageable, total != null ? total : 0L);
	}

	// @Override
	// public long closeBoardsByIds(List<? extends Long> ids) {
	// 	return queryFactory.update(board)
	// 		.set(board.accessPolicy, AccessPolicy.CLOSED)
	// 		.where(board.id.in(ids))
	// 		.execute();
	// }

	private List<OrderSpecifier<? extends Comparable>> getOrderSpecifier(Pageable pageable) {
		List<OrderSpecifier<? extends Comparable>> orders = new ArrayList<>();

		if (pageable.getSort().isSorted()) {
			for (Sort.Order sortOrder : pageable.getSort()) {
				// Spring의 Sort.Order → QueryDSL의 Order 변환
				Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
				String property = sortOrder.getProperty();

				OrderSpecifier<? extends Comparable> orderSpecifier = createOrderSpecifierWithReflection(property,
					direction);
				orders.add(orderSpecifier);
			}
		} else {
			// 디폴트 생성일 기준 내림차순
			orders.add(createOrderSpecifierWithReflection("createdAt", Order.DESC));
		}

		return orders;
	}

	private OrderSpecifier<? extends Comparable> createOrderSpecifierWithReflection(String property, Order direction) {
		// 동적 쿼리를 위한 PathBuilder, 리플랙션 사용
		PathBuilder<Board> pathBuilder = new PathBuilder<>(Board.class, "board");

		try {
			Field field = Board.class.getDeclaredField(property);
			Class<?> fieldType = field.getType();

			if (LocalDateTime.class.isAssignableFrom(fieldType)) {
				return new OrderSpecifier<>(direction, pathBuilder.getDateTime(property, LocalDateTime.class));
			} else if (String.class.isAssignableFrom(fieldType)) {
				return new OrderSpecifier<>(direction, pathBuilder.getString(property));
			} else if (Long.class.isAssignableFrom(fieldType) || long.class.equals(fieldType)) {
				return new OrderSpecifier<>(direction, pathBuilder.getNumber(property, Long.class));
			} else if (Integer.class.isAssignableFrom(fieldType) || int.class.equals(fieldType)) {
				return new OrderSpecifier<>(direction, pathBuilder.getNumber(property, Integer.class));
			} else if (Enum.class.isAssignableFrom(fieldType)) {
				return new OrderSpecifier<>(direction, pathBuilder.getString(property));
			}

		} catch (NoSuchFieldException e) {
			throw new CustomException(ErrorCode.INVALID_TYPE_VALUE);
		}
		return new OrderSpecifier<>(Order.DESC, pathBuilder.getDateTime("createdAt", LocalDateTime.class));
	}

	// 동적 검색 조건 생성
	private BooleanBuilder buildSearchConditions(BoardSearchCondition condition) {
		BooleanBuilder builder = new BooleanBuilder();
		// 현재 시간에 공개된 게시글만
		builder.and(
			board.openTime.isNull()
				.or(board.openTime.loe(LocalDateTime.now()))
		);
		// 공개 게시글이거나 오픈런 게시글의 경우 openLimit이 1이상인 경우만(유효한 상태의 게시글만)
		builder.and(
			board.accessPolicy.eq(AccessPolicy.OPEN)
				.or(board.accessPolicy.eq(AccessPolicy.FCFS).and(board.openLimit.gt(0)))
				.or(board.accessPolicy.eq(AccessPolicy.TIMEATTACK).and(board.openLimit.gt(0)))
		);

		// 통합 키워드 검색
		if (StringUtils.hasText(condition.getKeyword())) {
			BooleanExpression titleContains = board.title.containsIgnoreCase(condition.getKeyword());
			BooleanExpression contentContains = board.contents.containsIgnoreCase(condition.getKeyword());
			BooleanExpression hashtagContains = board.boardHashtagSet.any().hashtag.name.containsIgnoreCase(
				condition.getKeyword());
			builder.and(titleContains.or(contentContains).or(hashtagContains));
		} else {
			// 키워드가 없는 경우에만 개별 검색 사용
			// 제목으로 검색
			if (StringUtils.hasText(condition.getTitle())) {    // null체크, 길이 체크
				builder.and(board.title.containsIgnoreCase(condition.getTitle()));
			}
			// 내용으로 검색
			if (StringUtils.hasText(condition.getContents())) {
				builder.and(board.contents.containsIgnoreCase(condition.getContents()));
			}
		}
		// 게시글 타입 필터
		if (StringUtils.hasText(condition.getType())) {
			builder.and(board.type.eq(BoardType.from(condition.getType())));
		}
		// 게시글 상태 필터
		if (StringUtils.hasText(condition.getAccessPolicy())) {
			builder.and(board.accessPolicy.eq(AccessPolicy.from(condition.getAccessPolicy())));
		}
		// 가게명
		if (StringUtils.hasText(condition.getStoreName())) {
			builder.and(board.store.name.containsIgnoreCase(condition.getStoreName()));
		}
		// 작성자
		if (StringUtils.hasText(condition.getAuthorName())) {
			builder.and(board.user.nickname.containsIgnoreCase(condition.getAuthorName()));
		}
		// 기간 검색 필터
		if (condition.getDateRange().getCreatedFrom() != null && condition.getDateRange().getCreatedTo() != null) {
			builder.and(board.createdAt.between(
				condition.getDateRange().getCreatedFrom().atStartOfDay(),
				condition.getDateRange().getCreatedTo().atTime(23, 59, 59)
			));
		} else {
			// 생성일 이후
			if (condition.getDateRange().getCreatedFrom() != null) {    // goe(): A >= ?
				builder.and(board.createdAt.goe(condition.getDateRange().getCreatedFrom().atStartOfDay()));
			}
			// 생성일 이전
			if (condition.getDateRange().getCreatedTo() != null) {    // loe(): A <= ?
				builder.and(board.createdAt.loe(condition.getDateRange().getCreatedTo().atTime(23, 59, 59)));
			}

		}

		return builder;
	}
}

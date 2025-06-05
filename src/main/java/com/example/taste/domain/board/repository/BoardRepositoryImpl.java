package com.example.taste.domain.board.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardStatus;
import com.example.taste.domain.board.entity.BoardType;
import com.example.taste.domain.board.entity.QBoard;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class BoardRepositoryImpl implements BoardRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Board> searchBoardDetailList(List<Long> userIdList, String type, String status, String sort,
		String order,
		Pageable pageable) {
		QBoard board = QBoard.board;
		BooleanBuilder builder = new BooleanBuilder();
		if (userIdList != null && !userIdList.isEmpty()) {
			builder.and(board.user.id.in(userIdList));
		}
		try {
			if (type != null) {
				BoardType boardType = BoardType.from(type);
				builder.and(board.type.eq(boardType));
			}
			if (status != null) {
				BoardStatus boardStatus = BoardStatus.from(status);
				builder.and(board.status.eq(boardStatus));
			}
		} catch (IllegalArgumentException e) {

		}
		OrderSpecifier<? extends Comparable> orderSpecifier = getOrderSpecifier(sort, order, board);

		return queryFactory
			.selectFrom(board)
			.where(builder)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.orderBy(orderSpecifier)
			.fetch();
	}

	private OrderSpecifier<? extends Comparable> getOrderSpecifier(String sort, String order, QBoard board) {
		PathBuilder<Board> pathBuilder = new PathBuilder<>(Board.class, "board");

		if (sort == null || sort.isBlank()) {
			sort = "createdAt";
		}
		if (order == null || order.isBlank()) {
			order = "desc";
		}
		Order direction = order.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC;
		OrderSpecifier<? extends Comparable> orderSpecifier;
		switch (sort) {
			case "createdAt":
				orderSpecifier = new OrderSpecifier<>(
					direction,
					pathBuilder.getDate(sort, java.time.LocalDateTime.class)
				);
				break;
			case "title":
				orderSpecifier = new OrderSpecifier<>(
					direction,
					pathBuilder.getString(sort)
				);
				break;
			default:
				orderSpecifier = new OrderSpecifier<>(
					Order.DESC,
					pathBuilder.getDateTime(
						"createdAt",
						java.time.LocalDateTime.class)
				);
		}
		return orderSpecifier;
	}
}

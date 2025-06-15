package com.example.taste.domain.event.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.QBoard;
import com.example.taste.domain.board.entity.QLike;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.entity.QBoardEvent;
import com.example.taste.domain.event.entity.QEvent;
import com.example.taste.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	QEvent event = QEvent.event;
	QBoardEvent boardEvent = QBoardEvent.boardEvent;
	QBoard board = QBoard.board;
	QLike like = QLike.like;
	QUser user = QUser.user;

	public List<Event> findEndedEventList(LocalDate endDate) {

		List<Event> eventList = queryFactory
			.selectFrom(event)
			.where(event.endDate.eq(endDate))
			.fetch();

		return eventList;
	}

	public Optional<Board> findWinningBoard(Long eventId) {
		Long boardId = queryFactory
			.select(board.id)
			.from(boardEvent)
			.join(boardEvent.board, board)
			.leftJoin(board.likeList, like)
			.where(boardEvent.event.id.eq(eventId))
			.groupBy(board.id)
			.orderBy(like.count().desc(), board.createdAt.asc())
			.limit(1)
			.fetchOne();

		if (boardId == null) {
			return Optional.empty();
		}
		
		Board result = queryFactory
			.selectFrom(board)
			.join(board.user, user).fetchJoin()
			.where(board.id.eq(boardId))
			.fetchOne();

		return Optional.ofNullable(result);
	}

}

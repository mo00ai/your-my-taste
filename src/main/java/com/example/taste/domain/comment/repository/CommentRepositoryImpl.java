package com.example.taste.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.QBoard;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.entity.QComment;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Comment> findAllRootByBoard(Board board, Pageable pageable) {
		QBoard qBoard = QBoard.board;
		QComment qComment = QComment.comment;

		List<Comment> allRoot = queryFactory.selectFrom(qComment)
			.leftJoin(qComment.board, qBoard)
			.fetchJoin()
			.where(
				qComment.board.eq(board),
				qComment.root.isNull()
			)
			.orderBy(qComment.createdAt.asc())
			.distinct()
			.fetch();

		int here = (int)pageable.getOffset();
		int there = Math.min(here + pageable.getPageSize(), allRoot.size());
		List<Comment> sub = allRoot.subList(here, there);
		return new PageImpl<>(sub, pageable, allRoot.size());
	}

	// page가 이미 필요한 comment 객체를 가지고 있으므로, join을 하지 않아도 n+1이 발생하지 않음?
	@Override
	public List<Comment> findAllReplies(Page<Comment> page) {
		QComment qComment = QComment.comment;

		return queryFactory.selectFrom(qComment)
			.where(
				qComment.root.in(page.getContent())
			).orderBy(qComment.createdAt.asc()).distinct().fetch();
	}
}

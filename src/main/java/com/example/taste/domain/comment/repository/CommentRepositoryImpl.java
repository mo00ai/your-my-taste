package com.example.taste.domain.comment.repository;

import java.util.List;

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
	public List<Comment> findAllByBoard(Board board) {
		QBoard qBoard = QBoard.board;
		QComment qComment = QComment.comment;
		QComment qChild = new QComment("child"); // sql문에서 comment as chlid 이런 식으로 사용하게 됨
		QComment qParent = new QComment("parent");

		return queryFactory.selectFrom(qComment)
			.leftJoin(qComment.board, qBoard)
			.fetchJoin()
			.leftJoin(qComment.parent, qParent)
			.fetchJoin() // LEFT JOIN comment parent ON comment.parent_id = parent.id
			.leftJoin(qComment.children, qChild)
			.fetchJoin()
			.where(
				qComment.board.eq(board)
			)
			.distinct()
			.fetch();
	}
}

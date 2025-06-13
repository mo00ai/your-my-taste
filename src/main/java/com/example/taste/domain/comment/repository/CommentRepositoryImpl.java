package com.example.taste.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.entity.QComment;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Comment> findAllRootByBoard(Long boardId, Pageable pageable) {
		QComment qComment = QComment.comment;
		List<Comment> allRoot = queryFactory.selectFrom(qComment)
			.where(
				qComment.board.id.eq(boardId),
				qComment.root.isNull()
			)
			.orderBy(qComment.createdAt.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		return new PageImpl<>(allRoot, pageable, allRoot.size());
	}

	@Override
	public Slice<Comment> findChildComment(Long parentCommentId, Pageable pageable) {
		QComment qComment = QComment.comment;
		List<Comment> allChild = queryFactory.selectFrom(qComment)
			.where(
				qComment.parent.id.eq(parentCommentId)
			)
			.orderBy(qComment.createdAt.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = allChild.size() > pageable.getPageSize();
		allChild = hasNext ? allChild.subList(0, pageable.getPageSize()) : allChild;

		return new SliceImpl<>(allChild, pageable, hasNext);
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

package com.example.taste.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.taste.domain.comment.entity.Comment;

public interface CommentRepositoryCustom {
	Page<Comment> findAllRootByBoard(Long boardId, Pageable pageable);

	Slice<Comment> findChildComment(Long parentCommentId, Pageable pageable);

	List<Comment> findAllReplies(Page<Comment> page);
}

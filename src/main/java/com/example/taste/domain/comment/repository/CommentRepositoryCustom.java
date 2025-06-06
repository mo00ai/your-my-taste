package com.example.taste.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.comment.entity.Comment;

public interface CommentRepositoryCustom {
	Page<Comment> findAllRootByBoard(Board board, Pageable pageable);

	List<Comment> findAllReplies(Page<Comment> page);
}

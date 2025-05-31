package com.example.taste.domain.comment.repository;

import java.util.List;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.comment.entity.Comment;

public interface CommentRepositoryCustom {
	List<Comment> findAllByBoard(Board board);
}

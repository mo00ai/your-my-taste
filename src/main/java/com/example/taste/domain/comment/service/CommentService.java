package com.example.taste.domain.comment.service;

import org.springframework.stereotype.Service;

import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.entity.Comment;
import com.example.taste.domain.comment.repository.CommentRepository;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;

	public CreateCommentResponseDto createComment(CreateCommentRequestDto requestDto, Long boardsId) {
		Board board = new Board();
		User user = new User(); //세션에서 가져올 것

		Comment comment = Comment.builder()
			.content(requestDto.getContent())
			.comment(requestDto.getPareantComment())
			.board(board)
			.user(user)
			.build();

		Comment saved = commentRepository.save(comment);

		return new CreateCommentResponseDto(saved);
	}
}

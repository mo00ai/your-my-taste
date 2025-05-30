package com.example.taste.domain.comment.service;

import org.springframework.stereotype.Service;

import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.repository.CommentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {
	private final CommentRepository commentRepository;

	public CreateCommentResponseDto createComment(CreateCommentRequestDto requestDto, Long boardsId) {

	}
}

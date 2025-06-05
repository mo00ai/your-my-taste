package com.example.taste.domain.comment.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.comment.entity.Comment;

import lombok.Getter;

@Getter
public class CreateCommentResponseDto {
	private Long id;
	private Long boardId;
	private Long userId;
	private String contents;
	private LocalDateTime createdAt;

	public CreateCommentResponseDto(Comment comment) {
		this.id = comment.getId();
		this.boardId = comment.getBoard().getId();
		this.userId = comment.getUser().getId();
		this.contents = comment.getContents();
		this.createdAt = comment.getCreatedAt();
	}
}

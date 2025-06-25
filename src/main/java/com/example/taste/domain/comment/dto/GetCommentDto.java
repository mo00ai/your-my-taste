package com.example.taste.domain.comment.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.comment.entity.Comment;

import lombok.Getter;

@Getter
public class GetCommentDto {
	private Long id;
	private Long boardId;
	private Long userId;
	private String contents;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private LocalDateTime deletedAt;

	public GetCommentDto(Comment comment) {
		this.id = comment.getId();
		this.boardId = comment.getBoard().getId();
		this.userId = comment.getDeletedAt() == null ? comment.getUser().getId() : null;
		this.contents = comment.getDeletedAt() == null ? comment.getContents() : "삭제된 댓글입니다.";
		this.createdAt = comment.getCreatedAt();
		this.updatedAt = comment.getUpdatedAt();
		this.deletedAt = comment.getDeletedAt();
	}
}

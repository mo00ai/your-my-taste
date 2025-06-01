package com.example.taste.domain.comment.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.taste.domain.comment.entity.Comment;

import lombok.Getter;

@Getter
public class GetCommentDto {
	private Long id;
	private Long boardId;
	private Long userId;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<GetCommentDto> children = new ArrayList<>();

	public GetCommentDto(Comment comment) {
		this.id = comment.getId();
		this.boardId = comment.getBoard().getId();
		this.userId = comment.getUser().getId();
		this.content = comment.getContent();
		this.createdAt = comment.getCreatedAt();
		this.updatedAt = comment.getUpdatedAt();

		// 재귀적으로 자식 댓글도 DTO로 변환
		if (comment.getChildren() != null && !comment.getChildren().isEmpty()) {
			this.children = comment.getChildren().stream()
				.map(GetCommentDto::new)
				.collect(Collectors.toList());
		}
	}
}

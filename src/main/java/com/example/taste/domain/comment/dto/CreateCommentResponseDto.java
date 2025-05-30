package com.example.taste.domain.comment.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateCommentResponseDto {
	private Long id;
	private Long boardId;
	private Long userId;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

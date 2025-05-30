package com.example.taste.domain.comment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.dto.DeleteCommentResponseDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardsId}/comments")
public class CommentController {
	private final CommentService commentService;

	@PostMapping()
	public ResponseEntity<CommonResponse<CreateCommentResponseDto>> createComment(
		@RequestBody CreateCommentRequestDto requestDto,
		@PathVariable Long boardsId) {
		return ResponseEntity.ok(CommonResponse.ok(commentService.createComment(requestDto, boardsId)));
	}

	@PatchMapping("/{commentId}")
	public ResponseEntity<CommonResponse<UpdateCommentResponseDto>> updateComment(
		@RequestBody UpdateCommentRequestDto requestDto,
		@PathVariable Long commentId) {
		return ResponseEntity.ok(CommonResponse.ok(commentService.updateComment(requestDto, commentId)));
	}

	@PatchMapping("/{commentId}/delete")
	public ResponseEntity<CommonResponse<DeleteCommentResponseDto>> deleteComment(
		@PathVariable Long commentId) {
		return ResponseEntity.ok(CommonResponse.ok(commentService.deleteComment(commentId)));
	}
}

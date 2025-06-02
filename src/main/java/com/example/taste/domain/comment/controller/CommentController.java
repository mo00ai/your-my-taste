package com.example.taste.domain.comment.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.comment.dto.CreateCommentRequestDto;
import com.example.taste.domain.comment.dto.CreateCommentResponseDto;
import com.example.taste.domain.comment.dto.DeleteCommentResponseDto;
import com.example.taste.domain.comment.dto.GetCommentDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.service.CommentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardsId}/comments")
public class CommentController {
	private final CommentService commentService;

	@PostMapping
	public CommonResponse<CreateCommentResponseDto> createComment(
		@RequestBody CreateCommentRequestDto requestDto,
		@PathVariable Long boardsId) {
		return CommonResponse.created(commentService.createComment(requestDto, boardsId));
	}

	@PatchMapping("/{commentId}")
	public CommonResponse<UpdateCommentResponseDto> updateComment(
		@RequestBody UpdateCommentRequestDto requestDto,
		@PathVariable Long commentId) {
		return CommonResponse.ok(commentService.updateComment(requestDto, commentId));
	}

	@DeleteMapping("/{commentId}/delete")
	public CommonResponse<DeleteCommentResponseDto> deleteComment(
		@PathVariable Long commentId) {
		return CommonResponse.ok(commentService.deleteComment(commentId));
	}

	@GetMapping
	public CommonResponse<Page<GetCommentDto>> getAllCommentOfBoard(
		@PathVariable Long boardsId, @RequestParam(defaultValue = "1", required = false) int index) {
		return CommonResponse.ok(commentService.getAllCommentOfBoard(boardsId, index));
	}

	@GetMapping("/{commentId}")
	public CommonResponse<GetCommentDto> getComment(
		@PathVariable Long commentId) {
		return CommonResponse.ok(commentService.getComment(commentId));
	}
}

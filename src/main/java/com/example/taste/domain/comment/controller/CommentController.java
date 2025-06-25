package com.example.taste.domain.comment.controller;

import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.example.taste.domain.comment.dto.GetCommentDto;
import com.example.taste.domain.comment.dto.UpdateCommentRequestDto;
import com.example.taste.domain.comment.dto.UpdateCommentResponseDto;
import com.example.taste.domain.comment.service.CommentService;
import com.example.taste.domain.user.entity.CustomUserDetails;

@RestController
@RequiredArgsConstructor
@RequestMapping("/boards/{boardId}/comments")
public class CommentController {
	private final CommentService commentService;

	// 댓글 생성
	@PostMapping
	public CommonResponse<CreateCommentResponseDto> createComment(
		@RequestBody CreateCommentRequestDto requestDto,
		@PathVariable Long boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.created(commentService.createComment(requestDto, boardId, userDetails.getId()));
	}

	// 댓글 수정
	@PatchMapping("/{commentId}")
	public CommonResponse<UpdateCommentResponseDto> updateComment(
		@RequestBody UpdateCommentRequestDto requestDto,
		@PathVariable Long boardId,
		@PathVariable Long commentId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		return CommonResponse.ok(commentService.updateComment(requestDto, commentId, userDetails.getId()));
	}

	// 댓글 삭제(소프트)
	@DeleteMapping("/{commentId}")
	public CommonResponse<Void> deleteComment(
		@PathVariable Long commentId,
		@PathVariable Long boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		commentService.deleteComment(commentId, userDetails.getId());
		return CommonResponse.ok();
	}

	/*
	// 게시글에 모든 댓글 조회
	@GetMapping
	public CommonResponse<Page<GetCommentDto>> getAllCommentOfBoard(
		@PathVariable Long boardId, @RequestParam(defaultValue = "1", required = false) @Min(1) int index) {
		return CommonResponse.ok(commentService.getAllCommentOfBoard(boardId, index));
	}
	 */

	//루트 댓글만 가져오기
	@GetMapping
	public CommonResponse<Page<GetCommentDto>> getAllRootCommentOfBoard(
		@PathVariable Long boardId, @RequestParam(defaultValue = "1", required = false) @Min(1) int index) {
		return CommonResponse.ok(commentService.getAllRootCommentOfBoard(boardId, index));
	}

	//대댓글 가져오기
	@GetMapping("/{commentId}")
	public CommonResponse<Slice<GetCommentDto>> getChildComment(
		@PathVariable Long boardId, @PathVariable Long commentId,
		@RequestParam(defaultValue = "1", required = false) @Min(1) int index) {
		return CommonResponse.ok(commentService.getChildComment(commentId, index));
	}

	// 댓글 하나 조회
	@GetMapping("/comment/{commentId}")
	public CommonResponse<GetCommentDto> getComment(
		@PathVariable Long boardId,
		@PathVariable Long commentId) {
		return CommonResponse.ok(commentService.getComment(commentId));
	}
}

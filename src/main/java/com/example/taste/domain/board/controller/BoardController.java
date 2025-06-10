package com.example.taste.domain.board.controller;

import static com.example.taste.domain.board.dto.response.BoardSuccessCode.*;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.annotation.ImageValid;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.common.response.PageResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.dto.response.OpenRunBoardResponseDto;
import com.example.taste.domain.board.service.BoardService;
import com.example.taste.domain.board.service.LikeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/boards")
public class BoardController {

	private final BoardService boardService;
	private final LikeService likeService;

	@ImageValid
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CommonResponse<Void> createBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestPart("requestDto") BoardRequestDto requestDto,
		@RequestPart(value = "files", required = false) List<MultipartFile> files) throws IOException {
		boardService.createBoard(userDetails.getId(), requestDto, files);
		return CommonResponse.success(BOARD_CREATED);

	}

	// 게시글 단건 조회
	@GetMapping("/{boardId}")
	public CommonResponse<BoardResponseDto> findBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long boardId) {
		Long userId = userDetails.getId();
		BoardResponseDto responseDto = boardService.findBoard(boardId, userId);
		return CommonResponse.ok(responseDto);
	}

	// 단순 게시글 목록
	@GetMapping("/simple")
	public CommonResponse<?> findBoardList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PageableDefault(page = 0, size = 10) Pageable pageable
	) {
		List<BoardListResponseDto> responseDtoList = boardService.findBoardList(userDetails.getId(), pageable);
		return CommonResponse.ok(responseDtoList);
	}

	@GetMapping("/detailed")
	public CommonResponse<?> findBoardDetailList(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String type,
		@RequestParam(required = false) String status,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String order,
		Pageable pageable
	) {
		// TODO 기능 미구현
		List<BoardResponseDto> responseDtoList = boardService.findBoardsFromFollowingUsers(userDetails.getId(), type,
			status, sort,
			order,
			pageable);
		// TODO 반환
		return CommonResponse.ok(responseDtoList);
	}

	@GetMapping("/openRun")
	public CommonResponse<PageResponse<OpenRunBoardResponseDto>> findOpenRunBoardList(Pageable pageable) {
		return CommonResponse.ok(boardService.findOpenRunBoardList(pageable));
	}

	@PatchMapping("/{boardId}")
	public CommonResponse<Void> updateBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@Valid @RequestBody BoardUpdateRequestDto requestDto,
		@PathVariable Long boardId
	) throws IOException {
		boardService.updateBoard(userDetails.getId(), boardId, requestDto);
		return CommonResponse.success(BOARD_UPDATED);
	}

	@DeleteMapping("/{boardId}")
	public CommonResponse<Void> deleteBoard(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@PathVariable Long boardId) {
		boardService.deleteBoard(userDetails.getId(), boardId);
		return CommonResponse.success(BOARD_DELETED);
	}

	@PostMapping("/{boardId}/likes")
	public CommonResponse<Void> likeBoard(@PathVariable Long boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		likeService.likeBoard(userDetails.getId(), boardId);
		return CommonResponse.success(BOARD_LIKED);
	}

	@DeleteMapping("/{boardId}/likes")
	public CommonResponse<Void> unlikeBoard(@PathVariable Long boardId,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		likeService.unlikeBoard(userDetails.getId(), boardId);
		return CommonResponse.success(BOARD_UNLIKED);
	}
}

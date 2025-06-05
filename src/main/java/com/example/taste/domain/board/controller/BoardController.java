package com.example.taste.domain.board.controller;

import static com.example.taste.domain.board.dto.response.BoardSuccessCode.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.service.BoardService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/boards")
public class BoardController {

	private final BoardService boardService;

	@PostMapping
	public CommonResponse<Void> createBoard(@RequestBody BoardRequestDto requestDto) {
		Long storeId = 1L;
		Long userId = 1L;
		// TODO store, user 객체 받아오기
		boardService.createBoard(userId, requestDto.getStoreId(), requestDto);
		return CommonResponse.success(BOARD_CREATED);

	}

	// 게시글 단건 조회
	@GetMapping("/{boardId}")
	public CommonResponse<BoardResponseDto> findBoard(@PathVariable Long boardId) {
		BoardResponseDto responseDto = boardService.findBoard(boardId);
		return CommonResponse.ok(responseDto);
	}

	// 단순 게시글 목록
	@GetMapping("/simple")
	public CommonResponse<?> findBoardList(
		@PageableDefault(page = 0, size = 10) Pageable pageable
	) {

		// TODO user 코드 수정필요
		Long userId = 1L;
		List<BoardListResponseDto> responseDtoList = boardService.findBoardList(userId, pageable);
		return CommonResponse.ok(responseDtoList);
	}

	@GetMapping("/detailed")
	public CommonResponse<?> findBoardDetailList(
		@RequestParam(required = false) String type,
		@RequestParam(required = false) String status,
		@RequestParam(defaultValue = "createdAt") String sort,
		@RequestParam(defaultValue = "desc") String order,
		Pageable pageable
	) {
		// TODO user 코드 수정필요
		Long userId = 1L;
		// TODO 기능 미구현
		List<BoardResponseDto> responseDtoList = boardService.findBoardsFromFollowingUsers(userId, type, status, sort,
			order,
			pageable);
		// TODO 반환
		return CommonResponse.ok(responseDtoList);
	}

	@PatchMapping("/{boardId}")
	public CommonResponse<Void> updateBoard(@RequestBody BoardUpdateRequestDto requestDto,
		@PathVariable Long boardId
	) {
		// TODO user 코드 수정필요
		Long userId = 1L;
		boardService.updateBoard(userId, boardId, requestDto);
		return CommonResponse.success(BOARD_UPDATED);
	}

	@DeleteMapping("/{boardId}")
	public CommonResponse<Void> deleteBoard(@PathVariable Long boardId) {
		// TODO user 코드 수정필요
		Long userId = 1L;
		boardService.deleteBoard(userId, boardId);
		return CommonResponse.success(BOARD_DELETED);
	}

}

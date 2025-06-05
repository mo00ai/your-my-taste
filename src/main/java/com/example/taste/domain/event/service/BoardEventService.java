package com.example.taste.domain.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.service.BoardService;
import com.example.taste.domain.event.entity.BoardEvent;
import com.example.taste.domain.event.entity.Event;
import com.example.taste.domain.event.exception.EventErrorCode;
import com.example.taste.domain.event.repository.BoardEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardEventService {
	private final BoardEventRepository boardEventRepository;
	private final BoardService boardService;
	private final EventService eventService;

	@Transactional
	public void createBoardEvent(Long eventId, Long boardId, Long userId) {
		Event event = eventService.findById(eventId);
		Board board = boardService.findByBoardId(boardId);
		checkUser(board, userId);

		// 먼저 중복 체크
		boolean exists = boardEventRepository.existsByEventAndBoard(event, board);
		if (exists) {
			throw new CustomException(EventErrorCode.ALREADY_APPLIED);
		}
		BoardEvent boardEvent = BoardEvent.builder()
			.event(event)
			.board(board)
			.build();

		boardEventRepository.save(boardEvent);
	}

	@Transactional
	public void cancelEventApplication(Long eventId, Long boardId, Long userId) {
		Event event = eventService.findById(eventId);
		Board board = boardService.findByBoardId(boardId);
		checkUser(board, userId);
		BoardEvent boardEvent = boardEventRepository.findByEventAndBoard(event, board)
			.orElseThrow(() -> new CustomException(EventErrorCode.NOT_FOUND_BOARD_EVENT));

		// 1. 양방향 연관관계 해제
		boardEvent.unregister();

		// 2. 엔티티 삭제
		boardEventRepository.delete(boardEvent);
	}

	private void checkUser(Board board, Long userId) {
		if (!board.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
	}
}

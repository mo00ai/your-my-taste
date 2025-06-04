package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.request.HongdaeBoardRequestDto;
import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.mapper.BoardMapper;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

	private final BoardRepository boardRepository;
	// TODO 가게 정보, 유저 정보 가져오기
	// private final StoreService storeService;
	// private final UserService userService;

	@Transactional
	public void createBoard(BoardRequestDto requestDto) {
		// TODO 가게, 유저 주석 해제하기
		// User user = userService.findById(userId);
		// Store store = storeService.findById(storeId);
		User user = new User();
		Store store = new Store();
		if (requestDto instanceof NormalBoardRequestDto normalRequestDto) {
			Board entity = BoardMapper.toEntity(normalRequestDto, store, user);
			boardRepository.save(entity);

		} else if (requestDto instanceof HongdaeBoardRequestDto hongdaeRequestDto) {
			Board entity = BoardMapper.toEntity(hongdaeRequestDto, store, user);
			boardRepository.save(entity);

		} else {
			throw new IllegalArgumentException("지원하지 않는 게시글 타입입니다.");
		}

	}

	@Transactional(readOnly = true)
	public BoardResponseDto findBoard(Long boardId) {
		Board board = findByBoardId(boardId);
		return new BoardResponseDto(board);
	}

	@Transactional
	public void updateBoard(Long userId, Long boardId, BoardUpdateRequestDto requestDto) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		board.update(requestDto);

	}

	@Transactional
	public void deleteBoard(Long userId, Long boardId) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		board.softDelete();
	}

	@Transactional(readOnly = true)
	protected Board findByBoardId(Long boardId) {
		// deleteAt이 null인 유효한 게시물만 조회
		return boardRepository.findActiveBoard(boardId)
			.orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));
	}

	// 게시물 작성자와 현재 사용자가 일치하는지 검증
	private void checkUser(Long userId, Board board) {
		if (!board.getUser().getId().equals(userId)) {
			throw new CustomException(ErrorCode.UNAUTHORIZED);
		}
	}

	// 게시물 목록 상세 조회
	@Transactional(readOnly = true)
	public List<BoardResponseDto> findBoardsFromFollowingUsers(Long userId, String type, String status, String sort,
		String order,
		Pageable pageable) {

		/**
		 * TODO 구현할 기능 
		 * 현재 사용자id로 팔로우중인 사람들의 게시물 리스트 조회
		 * userService 코드 필요
		 */
		List<Long> userFollowList = new ArrayList<>();

		List<Board> searchBoardList = boardRepository.searchBoardDetailList(userFollowList, type, status, sort, order,
			pageable);
		return searchBoardList.stream()
			.map(BoardResponseDto::new)
			.toList();
	}

	// 게시물 목록 조회(게시글 제목, 작성자명, 가게명, 이미지 url)
	@Transactional(readOnly = true)
	public List<BoardListResponseDto> findBoardList(Long userId, Pageable pageable) {

		List<Long> userFollowList = new ArrayList<>();
		return boardRepository.findBoardListDtoByUserIdList(userFollowList, pageable);
	}
	
}

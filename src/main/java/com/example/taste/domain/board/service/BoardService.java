package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.example.taste.domain.board.mapper.BoardMapper;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.service.BoardImageService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.service.StoreService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

	private final BoardImageService boardImageService;
	private final BoardRepository boardRepository;
	private final StoreService storeService;
	private final UserService userService;
	private final PkService pkService;
	private final HashtagService hashtagService;
	private final EntityFetcher entityFetcher;

	@Transactional
	public void createBoard(Long userId, BoardRequestDto requestDto, List<MultipartFile> files) throws
		IOException {
		User user = entityFetcher.getUserOrThrow(userId);
		Store store = storeService.findById(requestDto.getStoreId());

		if (requestDto instanceof NormalBoardRequestDto normalRequestDto) {
			Board entity = BoardMapper.toEntity(normalRequestDto, store, user);
			if (normalRequestDto.getHashtagList() != null && !normalRequestDto.getHashtagList().isEmpty()) {
				hashtagService.applyHashtagsToBoard(entity, normalRequestDto.getHashtagList());
			}
			boardRepository.save(entity);
			pkService.savePkLog(userId, PkType.POST);
			if (files != null && !files.isEmpty()) {
				boardImageService.saveBoardImages(entity, files);
			}

		} else if (requestDto instanceof OpenRunBoardRequestDto openRunBoardRequestDto) {
			Board entity = BoardMapper.toEntity(openRunBoardRequestDto, store, user);
			// 해시태그 적용
			if (openRunBoardRequestDto.getHashtagList() != null && !openRunBoardRequestDto.getHashtagList().isEmpty()) {
				hashtagService.applyHashtagsToBoard(entity, openRunBoardRequestDto.getHashtagList());
			}
			boardRepository.save(entity);
			pkService.savePkLog(userId, PkType.POST);
			if (files != null && !files.isEmpty()) {
				boardImageService.saveBoardImages(entity, files);
			}

		} else {
			throw new CustomException(BoardErrorCode.BOARD_TYPE_NOT_FOUND);
		}

	}

	@Transactional(readOnly = true)
	public BoardResponseDto findBoard(Long boardId) {
		Board board = findByBoardId(boardId);
		return new BoardResponseDto(board);
	}

	@Transactional
	public void updateBoard(Long userId, Long boardId, BoardUpdateRequestDto requestDto) throws IOException {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		board.update(requestDto);
	}

	@Transactional
	public void deleteBoard(Long userId, Long boardId) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		board.softDelete();
		boardImageService.deleteBoardImages(board);
	}

	@Transactional(readOnly = true)
	public Board findByBoardId(Long boardId) {
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
		// 쿼리로 바로 dto프로젝션
		return boardRepository.findBoardListDtoByUserIdList(userFollowList, pageable);
	}

	// 게시글에 특정해시태그 삭제(단건 삭제)
	@Transactional
	public void removeHashtagFromBoard(Long userId, Long boardId, String hashtagName) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		hashtagService.removeHashtagFromBoard(board, hashtagName);
	}

	// 게시글에 해시태그 전부 삭제
	@Transactional
	public void removeAllHashtagsFromBoard(Long userId, Long boardId) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		hashtagService.clearBoardHashtags(board);
	}
}

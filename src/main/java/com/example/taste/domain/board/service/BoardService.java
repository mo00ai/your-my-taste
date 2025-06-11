package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.exception.BoardErrorCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.exception.ErrorCode;
import com.example.taste.common.response.PageResponse;
import com.example.taste.common.service.RedisService;
import com.example.taste.common.util.EntityFetcher;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.dto.response.OpenRunBoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardStatus;
import com.example.taste.domain.board.entity.BoardType;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.example.taste.domain.board.mapper.BoardMapper;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.service.BoardImageService;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.service.StoreService;
import com.example.taste.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

	private final BoardImageService boardImageService;
	private final BoardRepository boardRepository;
	private final StoreService storeService;
	private final EntityFetcher entityFetcher;
	private final PkService pkService;
	private final HashtagService hashtagService;
	private final RedisService redisService;
	private final SimpMessagingTemplate messagingTemplate;

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
			// 포스팅 횟수 증가
			user.increasePostingCnt();
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

	@Transactional
	public BoardResponseDto findBoard(Long boardId, Long userId) {
		Board board = findByBoardId(boardId);

		if (board.getType() == BoardType.N) {
			return new BoardResponseDto(board);
		}

		// 게시글 공개시간 전이면 error
		if (LocalDateTime.now().isBefore(board.getOpenTime())) {
			throw new CustomException(BOARD_NOT_YET_OPEN);
		}

		// 비공개 게시글이면 error
		if (board.getStatus() == BoardStatus.CLOSED) {
			throw new CustomException(CLOSED_BOARD);
		}

		// 타임어택 게시글이면 공개시간 만료 검증 (스케줄링 누락 방지)
		if (board.getStatus() == BoardStatus.TIMEATTACK) {
			board.validateAndCloseIfExpired();
		}

		// 선착순 공개 게시글이면 순위 검증
		if (board.getStatus() == BoardStatus.FCFS) {
			tryEnterFcfsQueue(board, userId);
		}

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
		redisService.deleteZSetKey(OPENRUN_KEY_PREFIX + board.getId());
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
	@Transactional(readOnly = true) // XXX 게시글 목록을 상세조회하는 메서드가 필요한가요?
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
	// XXX 지금은 팔로우중인 유저의 게시글만 조회하려는 거 같은데 전체 유저의 게시글 조회가 낫지 않을까요?
	// XXX dto 이름을 BoardListResponseDto -> BoardResponseDto로 변경하는 건 어떄요?
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

	// 오픈런 게시글 목록 조회
	// 클라이언트에서 조회 후 소켓 연결 요청
	public PageResponse<OpenRunBoardResponseDto> findOpenRunBoardList(Pageable pageable) {
		Page<Board> boards = boardRepository.findByTypeEqualsAndStatusIsFalse(BoardType.O, BoardStatus.CLOSED,
			pageable);

		Page<OpenRunBoardResponseDto> dtos = boards.map(board -> {
			Long zSetSize = null;
			if (board.getStatus() == BoardStatus.FCFS) {
				zSetSize = redisService.getZSetSize(OPENRUN_KEY_PREFIX + board.getId());
			}

			return OpenRunBoardResponseDto.create(board, zSetSize);
		});

		return PageResponse.from(dtos);
	}

	// 게시글에 해시태그 전부 삭제
	@Transactional
	public void removeAllHashtagsFromBoard(Long userId, Long boardId) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		hashtagService.clearBoardHashtags(board);
	}

	public void tryEnterFcfsQueue(Board board, Long userId) {
		String key = OPENRUN_KEY_PREFIX + board.getId();

		// ZSet 크기가 open limit을 초과하면 error로 메시지 전달
		long size = redisService.getZSetSize(key);
		if (size >= board.getOpenLimit()) {
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}

		// 순위가 없는 유저만 ZSet에 insert
		if (!redisService.hasRankInZSet(key, userId)) {
			redisService.addToZSet(key, userId, System.currentTimeMillis());

			// 클라이언트에 잔여 인원 전송
			String destination = "topic/openrun/board/" + board.getId();
			long remainingSlot = board.getOpenLimit() - redisService.getZSetSize(key);
			remainingSlot = remainingSlot > 0 ? remainingSlot : 0;
			messagingTemplate.convertAndSend(destination, remainingSlot);
		}

		// 동시성 문제로 openLimit 보다 초과 저장된 데이터 삭제
		if (redisService.getRank(key, userId) >= board.getOpenLimit()) {
			redisService.removeFromZSet(key, userId);
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}
	}
}

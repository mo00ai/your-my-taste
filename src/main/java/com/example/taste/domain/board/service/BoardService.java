package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.exception.BoardErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
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
import com.example.taste.domain.board.dto.search.BoardSearchCondition;
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
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

	private final BoardImageService boardImageService;
	private final BoardRepository boardRepository;
	private final StoreService storeService;
	private final PkService pkService;
	private final HashtagService hashtagService;
	private final EntityFetcher entityFetcher;
	private final RedisService redisService;
	private final SimpMessagingTemplate messagingTemplate;
	private final UserRepository userRepository;

	@Transactional
	public Long createBoard(Long userId, BoardRequestDto requestDto, List<MultipartFile> files) throws
		IOException {
		User user = entityFetcher.getUserOrThrow(userId);
		Store store = storeService.findById(requestDto.getStoreId());
		Long boardId = 0L; // aop 용

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

			boardId = entity.getId();

		} else if (requestDto instanceof OpenRunBoardRequestDto openRunBoardRequestDto) {
			Board entity = BoardMapper.toEntity(openRunBoardRequestDto, store, user);
			// 포스팅 횟수 증가
			int updatedUserCnt = userRepository.increasePostingCount(user.getId(), user.getLevel().getPostingLimit());
			if (updatedUserCnt == 0) {
				throw new CustomException(POSTING_COUNT_OVERFLOW);
			}
			// 해시태그 적용
			if (openRunBoardRequestDto.getHashtagList() != null && !openRunBoardRequestDto.getHashtagList().isEmpty()) {
				hashtagService.applyHashtagsToBoard(entity, openRunBoardRequestDto.getHashtagList());
			}
			boardRepository.save(entity);
			pkService.savePkLog(userId, PkType.POST);
			if (files != null && !files.isEmpty()) {
				boardImageService.saveBoardImages(entity, files);
			}
			// TODO 포스팅한 유저를 팔로우하고 있는 유저들에게 알림 전송 @김채진 - AOP 로 자동 처리 합니다.

			boardId = entity.getId();

		} else {
			throw new CustomException(BoardErrorCode.BOARD_TYPE_NOT_FOUND);
		}
		return boardId;
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
		if (board.getStatus() == BoardStatus.FCFS) {
			redisService.deleteZSetKey(OPENRUN_KEY_PREFIX + board.getId());
		}
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

	// 키워드 기반 게시물 목록 조회
	public PageResponse<BoardListResponseDto> searchBoards(BoardSearchCondition conditionDto, Pageable pageable) {
		Page<BoardListResponseDto> page = boardRepository.searchBoardsByKeyword(conditionDto,
			pageable);
		return PageResponse.from(page);

	}

	// 나와 내 팔로워 게시물 목록 조회(게시글 제목, 작성자명, 가게명, 이미지 url) TODO 쿼리수정해야함
	@Transactional(readOnly = true)
	public PageResponse<BoardListResponseDto> findBoardList(Long userId, Pageable pageable) {
		List<Long> userFollowList = userRepository.findFollowingIds(userId);
		userFollowList.add(userId);
		// 쿼리로 바로 dto프로젝션
		Page<BoardListResponseDto> boardPage = boardRepository.findBoardListDtoByUserIdList(
			userFollowList, pageable);
		return PageResponse.from(boardPage);
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
		Page<Board> boards = boardRepository.findByTypeEqualsAndStatusIn(BoardType.O,
			List.of(BoardStatus.FCFS, BoardStatus.TIMEATTACK),
			pageable);

		Page<OpenRunBoardResponseDto> dtos = boards.map(board -> {
			Long remainingSlot = null;
			if (board.getStatus() == BoardStatus.FCFS) {
				long zSetSize = redisService.getZSetSize(OPENRUN_KEY_PREFIX + board.getId());
				remainingSlot = Math.max(0, board.getOpenLimit() - zSetSize);
			}

			return OpenRunBoardResponseDto.create(board, remainingSlot);
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

	// 선착순 queue에 데이터 insert
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
			String destination = "/topic/openrun/board/" + board.getId();
			long remainingSlot = Math.max(0, board.getOpenLimit() - redisService.getZSetSize(key));
			messagingTemplate.convertAndSend(destination, remainingSlot);
		}

		// 동시성 문제로 openLimit 보다 초과 저장된 데이터 삭제
		Long rank = redisService.getRank(key, userId);
		if (rank != null && rank >= board.getOpenLimit()) {
			redisService.removeFromZSet(key, userId);
			throw new CustomException(EXCEED_OPEN_LIMIT);
		}
	}
}

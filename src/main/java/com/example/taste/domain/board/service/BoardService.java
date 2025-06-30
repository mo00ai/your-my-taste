package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.auth.exception.AuthErrorCode.*;
import static com.example.taste.domain.board.exception.BoardErrorCode.*;
import static com.example.taste.domain.store.exception.StoreErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.response.PageResponse;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.dto.response.OpenRunBoardQueryDto;
import com.example.taste.domain.board.dto.response.OpenRunBoardResponseDto;
import com.example.taste.domain.board.dto.search.BoardSearchCondition;
import com.example.taste.domain.board.entity.AccessPolicy;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardType;
import com.example.taste.domain.board.factory.BoardCreationStrategyFactory;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.board.strategy.BoardCreationStrategy;
import com.example.taste.domain.image.exception.ImageErrorCode;
import com.example.taste.domain.image.service.BoardImageService;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationCategory;
import com.example.taste.domain.notification.entity.NotificationType;
import com.example.taste.domain.pk.enums.PkType;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

	private final BoardImageService boardImageService;
	private final BoardRepository boardRepository;
	private final PkService pkService;
	private final StoreRepository storeRepository;
	private final HashtagService hashtagService;
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final BoardCreationStrategyFactory strategyFactory;
	private final EntityManager entityManager;
	private final ApplicationEventPublisher eventPublisher;
	private final BoardCacheService boardCacheService;
	private final FcfsQueueService fcfsQueueService;

	@Transactional
	public void createBoard(Long userId, BoardRequestDto requestDto, List<MultipartFile> files) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));
		Store store = storeRepository.findById(requestDto.getStoreId())
			.orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

		BoardCreationStrategy strategy = strategyFactory.getStrategy(BoardType.from(requestDto.getType()));
		Board entity = strategy.createBoard(requestDto, store, user);
		if (requestDto.getHashtagList() != null && !requestDto.getHashtagList().isEmpty()) {
			hashtagService.applyHashtagsToBoard(entity, requestDto.getHashtagList());
		}
		Board saved = boardRepository.save(entity);

		// 오픈런 게시글 카운팅
		if (!saved.isNBoard()) {
			int updatedUserCnt = userRepository.increasePostingCount(user.getId(), user.getLevel().getPostingLimit());
			entityManager.refresh(user);

			if (updatedUserCnt == 0) {
				throw new CustomException(POSTING_COUNT_OVERFLOW);
			}
		}
		pkService.savePkLog(userId, PkType.POST);
		try {
			if (files != null && !files.isEmpty()) {
				boardImageService.saveBoardImages(entity, files);
			}
		} catch (IOException e) {
			throw new CustomException(ImageErrorCode.FAILED_WRITE_FILE);
		}

		eventPublisher.publishEvent(NotificationPublishDto.builder()
			.userId(user.getId())
			.category(NotificationCategory.BOARD)
			.type(NotificationType.CREATE)
			.redirectionUrl("/board")
			.redirectionEntityId(saved.getId())
			.build());
	}

	public BoardResponseDto findBoard(Long boardId, Long userId) {
		Board board = boardRepository.findActiveBoard(boardId)
			.orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(NOT_FOUND_USER));

		// 게시글 유효성 검증
		validateBoard(board, user);

		// 캐시 데이터 반환
		if (board.getAccessPolicy().isTimeAttack()) {
			return boardCacheService.getInMemoryCache(board);
		}
		if (board.getAccessPolicy().isFcfs()) {
			return boardCacheService.getRedisCache(board);
		}

		return new BoardResponseDto(board);
	}

	public void validateBoard(Board board, User user) {
		if (board.isNBoard()) {
			return;
		}

		// 게시글 작성자거나 관리자이면 리턴
		if (board.getUser().isSameUser(user.getId()) || user.isAdmin()) {
			return;
		}

		// 게시글 공개시간 전이면 error
		if (!board.isOpenTimeNow()) {
			throw new CustomException(BOARD_NOT_YET_OPEN);
		}

		// 비공개 게시글이면 error
		if (board.getAccessPolicy().isClosed()) {
			throw new CustomException(CLOSED_BOARD);
		}

		// 타임어택 게시글이면 공개시간 만료 검증 (스케줄링 누락 방지)
		if (board.getAccessPolicy().isTimeAttack() && board.isExpired()) {
			throw new CustomException(CLOSED_BOARD);
		}

		// 선착순 공개 게시글이면 순위 검증
		if (board.getAccessPolicy().isFcfs()) {
			fcfsQueueService.tryEnterFcfsQueueByRedisson(board, user);
		}
	}

	@Transactional
	public void updateBoard(Long userId, Long boardId, BoardUpdateRequestDto requestDto) throws IOException {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		board.update(requestDto);
		if (!board.isNBoard()) {
			boardCacheService.evictCache(board);
		}
	}

	@Transactional
	public void deleteBoard(Long userId, Long boardId) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		if (board.getAccessPolicy().isFcfs()) {
			redisService.deleteKey(OPENRUN_KEY_PREFIX + board.getId());
		}
		board.softDelete();
		boardImageService.deleteBoardImages(board);
		if (!board.isNBoard()) {
			boardCacheService.evictCache(board);
		}
	}

	@Transactional(readOnly = true)
	public Board findByBoardId(Long boardId) {
		// deleteAt이 null인 유효한 게시물만 조회
		return boardRepository.findActiveBoard(boardId)
			.orElseThrow(() -> new CustomException(BOARD_NOT_FOUND));
	}

	// 게시물 작성자와 현재 사용자가 일치하는지 검증
	private void checkUser(Long userId, Board board) {
		if (!board.getUser().isSameUser(userId)) {
			throw new CustomException(UNAUTHORIZED);
		}
	}

	// 키워드 기반 게시물 목록 조회
	public PageResponse<BoardListResponseDto> searchBoards(BoardSearchCondition conditionDto, Pageable pageable) {
		Page<BoardListResponseDto> page = boardRepository.searchBoardsByKeyword(conditionDto,
			pageable);
		return PageResponse.from(page);

	}

	// 나와 내 팔로워 게시물 목록 조회(게시글 제목, 작성자명, 가게명, 이미지 url)
	@Transactional(readOnly = true)
	public PageResponse<BoardListResponseDto> findBoardList(Long userId, Pageable pageable) {
		List<Long> userFollowList = userRepository.findFollowingIds(userId); // 내 팔로워
		userFollowList.add(userId);    // 나도 포함
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
	// 클라이언트에서 조회 후 소켓 연결 요청하는 시나리오
	public PageResponse<OpenRunBoardResponseDto> findOpenRunBoardList(Pageable pageable) {
		Page<OpenRunBoardQueryDto> dtos = boardRepository.findUndeletedBoardByTypeAndPolicy(BoardType.O,
			List.of(AccessPolicy.FCFS, AccessPolicy.TIMEATTACK), pageable);

		Page<OpenRunBoardResponseDto> result = dtos.map(dto -> {
			boolean isClosed = dto.getOpenTime().isAfter(LocalDateTime.now());
			Long remainingSlot = null;
			Integer openLimit = dto.getOpenLimit();

			if (dto.getAccessPolicy().isFcfs() && !isClosed) {
				long zSetSize = redisService.getZSetSize(OPENRUN_KEY_PREFIX + dto.getBoardId());
				remainingSlot = Math.max(0, dto.getOpenLimit() - zSetSize);
			}

			if (isClosed) {
				openLimit = null;
			}

			return OpenRunBoardResponseDto.createFromDto(dto, openLimit, remainingSlot);
		});

		return PageResponse.from(result);
	}

	// 게시글에 해시태그 전부 삭제
	@Transactional
	public void removeAllHashtagsFromBoard(Long userId, Long boardId) {
		Board board = findByBoardId(boardId);
		checkUser(userId, board);
		hashtagService.clearBoardHashtags(board);
	}

	@Transactional
	public List<Long> findExpiredTimeAttackBoardIds(AccessPolicy policy) {
		return boardRepository.findExpiredTimeAttackBoardIds(policy);
	}

	@Transactional
	public long closeTimeAttackBoardsByIds(List<? extends Long> ids) {
		long closedCnt = boardRepository.closeBoardsByIds(ids);
		boardCacheService.evictTimeAttackCaches(ids);
		return closedCnt;
	}
}

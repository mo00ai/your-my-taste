package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.domain.board.exception.BoardErrorCode.*;
import static com.example.taste.domain.user.exception.UserErrorCode.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
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
import com.example.taste.config.KoreanTextProcessor;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.BoardUpdateRequestDto;
import com.example.taste.domain.board.dto.response.BoardListResponseDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
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
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Role;
import com.example.taste.domain.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

	private final BoardImageService boardImageService;
	private final BoardRepository boardRepository;
	private final PkService pkService;
	private final HashtagService hashtagService;
	private final EntityFetcher entityFetcher;
	private final RedisService redisService;
	private final SimpMessagingTemplate messagingTemplate;
	private final UserRepository userRepository;
	private final BoardCreationStrategyFactory strategyFactory;
	private final EntityManager entityManager;
	private final ApplicationEventPublisher eventPublisher;
	private final KoreanTextProcessor processor;

	@Transactional
	public void createBoard(Long userId, BoardRequestDto requestDto, List<MultipartFile> files) {
		User user = entityFetcher.getUserOrThrow(userId);
		Store store = entityFetcher.getStoreOrThrow(requestDto.getStoreId());

		BoardCreationStrategy strategy = strategyFactory.getStrategy(BoardType.from(requestDto.getType()));
		Board entity = strategy.createBoard(requestDto, store, user);
		if (requestDto.getHashtagList() != null && !requestDto.getHashtagList().isEmpty()) {
			hashtagService.applyHashtagsToBoard(entity, requestDto.getHashtagList());
		}
		// 검색용 인덱스 추가
		processAndSetSearchKeywords(entity);
		Board saved = boardRepository.save(entity);

		// 오픈런 게시글 카운팅
		if (BoardType.from(requestDto.getType()).equals(BoardType.O)) {
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

	@Transactional
	public BoardResponseDto findBoard(Long boardId, Long userId) {
		Board board = findByBoardId(boardId);
		User user = entityFetcher.getUserOrThrow(userId);

		if (board.getType() == BoardType.N) {
			return new BoardResponseDto(board);
		}

		if (board.getUser().isSameUser(userId) || user.getRole() == Role.ADMIN) {
			return new BoardResponseDto(board);
		}

		// 게시글 공개시간 전이면 error
		if (LocalDateTime.now().isBefore(board.getOpenTime())) {
			throw new CustomException(BOARD_NOT_YET_OPEN);
		}

		// 비공개 게시글이면 error
		if (board.getAccessPolicy() == AccessPolicy.CLOSED) {
			throw new CustomException(CLOSED_BOARD);
		}

		// 타임어택 게시글이면 공개시간 만료 검증 (스케줄링 누락 방지)
		if (board.getAccessPolicy() == AccessPolicy.TIMEATTACK) {
			board.validateAndCloseIfExpired();
		}

		// 선착순 공개 게시글이면 순위 검증
		if (board.getAccessPolicy() == AccessPolicy.FCFS) {
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
		if (board.getAccessPolicy() == AccessPolicy.FCFS) {
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
	// 클라이언트에서 조회 후 소켓 연결 요청
	public PageResponse<OpenRunBoardResponseDto> findOpenRunBoardList(Pageable pageable) {
		Page<Board> boards = boardRepository.findByTypeEqualsAndAccessPolicyInAndDeletedAtIsNull(BoardType.O,
			List.of(AccessPolicy.FCFS, AccessPolicy.TIMEATTACK), pageable);

		Page<OpenRunBoardResponseDto> dtos = boards.map(board -> {
			Long remainingSlot = null;
			if (board.getAccessPolicy() == AccessPolicy.FCFS) {
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
			String destination = "/sub/openrun/board/" + board.getId();
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

	// 검색용 인덱스 필드 초기화
	private void processAndSetSearchKeywords(Board board) {
		// 제목 + 내용 결합
		String combinedText = board.getTitle() + " " + board.getContents();

		// OKT로 키워드 추출
		Set<String> keywords = processor.extractSearchKeywords(combinedText);
		List<String> nouns = processor.extractNouns(combinedText);
		List<String> phrases = processor.extractPhrases(combinedText);

		// 중복 제거 및 필터링
		Set<String> cleanNouns = nouns.stream()
			.filter(noun -> noun.length() >= 2)
			.collect(Collectors.toSet());

		Set<String> cleanPhrases = phrases.stream()
			.filter(phrase -> phrase.length() >= 3)
			.filter(phrase -> phrase.split("\\s+").length <= 3)  // 3단어 이하만
			.collect(Collectors.toSet());

		// 데이터베이스 저장용 문자열로 변환
		board.setSearchKeywords(String.join(" ", keywords));
		board.setSearchNouns(String.join(" ", cleanNouns));
		board.setSearchPhrases(String.join(" ", cleanPhrases));

	}
}

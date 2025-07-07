package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.entity.AccessPolicy.CLOSED;
import static com.example.taste.domain.board.entity.AccessPolicy.FCFS;
import static com.example.taste.domain.board.entity.AccessPolicy.OPEN;
import static com.example.taste.domain.board.entity.AccessPolicy.TIMEATTACK;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.anyInt;
import static org.mockito.BDDMockito.anyLong;
import static org.mockito.BDDMockito.anyString;
import static org.mockito.BDDMockito.doNothing;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.exception.CustomException;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.common.service.RedisService;
import com.example.taste.config.KoreanTextProcessor;
import com.example.taste.domain.board.dto.request.BoardRequestDto;
import com.example.taste.domain.board.dto.request.NormalBoardRequestDto;
import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.entity.BoardType;
import com.example.taste.domain.board.exception.BoardErrorCode;
import com.example.taste.domain.board.factory.BoardCreationStrategyFactory;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.image.service.BoardImageService;
import com.example.taste.domain.pk.service.PkService;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.exception.StoreErrorCode;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.store.service.StoreService;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.enums.Level;
import com.example.taste.domain.user.enums.Role;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
public class BoardServiceUnitTest {
	@Spy
	@InjectMocks
	private BoardService boardService;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private BoardImageService boardImageService;
	@Mock
	private StoreService storeService;
	@Mock
	private PkService pkService;
	@Mock
	private HashtagService hashtagService;
	@Mock
	private RedisService redisService;
	@Mock
	private SimpMessagingTemplate messagingTemplate;
	@Mock
	private UserRepository userRepository;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private BoardCreationStrategyFactory strategyFactory;
	@Mock
	private KoreanTextProcessor processor;
	@Mock
	private ApplicationEventPublisher eventPublisher;
	@Mock
	private EntityManager entityManager;
	@Mock
	private RedissonClient redissonClient;
	@Mock
	private BoardCacheService boardCacheService;
	@Mock
	private FcfsQueueService fcfsQueueService;

	@DisplayName("게시글 조회 실패(오픈런 게시글 - 공개 전 게시글!)")
	@Test
	public void validateBoard_whenNotOpenYet_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user1 = UserFixture.create(image);
		User user2 = UserFixture.create(image);
		ReflectionTestUtils.setField(user1, "id", 1L);
		ReflectionTestUtils.setField(user2, "id", 2L);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		Board board = BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
			LocalDateTime.now().plusDays(1), store, user1);
		BoardResponseDto dto = new BoardResponseDto(board);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateOBoard(dto, user2);
		});
	}

	@DisplayName("게시글 조회 실패(오픈런 게시글 - 비공개 게시글!)")
	@Test
	public void validateBoard_whenClosedBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user1 = UserFixture.create(image);
		User user2 = UserFixture.create(image);
		ReflectionTestUtils.setField(user1, "id", 1L);
		ReflectionTestUtils.setField(user2, "id", 2L);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		Board board = BoardFixture.createOBoard("title", "contents", "O", CLOSED.name(), 10,
			LocalDateTime.now().plusDays(1), store, user1);
		BoardResponseDto dto = new BoardResponseDto(board);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateOBoard(dto, user2);
		});
	}

	@DisplayName("게시글 조회 실패(오픈런 게시글 - 타임어택 공개시간 만료!)")
	@Test
	public void validateBoard_whenTimeExceededBoard_ThrowException() {
		// given
		Image image = ImageFixture.create();
		User user1 = UserFixture.create(image);
		User user2 = UserFixture.create(image);
		ReflectionTestUtils.setField(user1, "id", 1L);
		ReflectionTestUtils.setField(user2, "id", 2L);
		Category category = CategoryFixture.create();
		Store store = StoreFixture.create(category);
		Board board = BoardFixture.createOBoard("title", "contents", "O", TIMEATTACK.name(), 10,
			LocalDateTime.now().minusDays(1), store, user1);
		BoardResponseDto dto = new BoardResponseDto(board);

		// when, then
		assertThrows(CustomException.class, () -> {
			boardService.validateOBoard(dto, user2);
		});
	}

	@DisplayName("게시글 조회 실패(오픈런 게시글 - FCFS 선착순 인원 초과!)")
	@Test
	public void findBoard_withFcfsCountExceeded_throwsException() throws InterruptedException {
		// given
		Long boardId = 1L;
		Long boardUserId = 999L;
		Long user1Id = 2L;
		Long user2Id = 3L;

		User writer = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(writer, "id", boardUserId);
		Store store = StoreFixture.create(CategoryFixture.create());

		User user1 = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user1, "id", user1Id);
		User user2 = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user2, "id", user2Id);

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "오픈런 선착순 마감 테스트!");
		ReflectionTestUtils.setField(dto, "contents", "인원 마감!");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", FCFS.toString());
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusMinutes(1));
		ReflectionTestUtils.setField(dto, "openLimit", 1);

		Board board = BoardFixture.createFcfsOBoard(dto, store, writer);
		RLock rLock = mock(RLock.class);

		// stub
		given(boardRepository.findActiveBoard(boardId)).willReturn(Optional.of(board));
		given(boardCacheService.getOrSetCache(boardId))
			.willReturn(BoardResponseDto.builder().entity(board).build());

		// 작성자는 조회 가능
		given(userRepository.findById(boardUserId)).willReturn(Optional.of(writer));
		BoardResponseDto result1 = boardService.findBoard(boardId, boardUserId);
		assertNotNull(result1);

		// user1은 성공
		given(userRepository.findById(user1Id)).willReturn(Optional.of(user1));
		doNothing().when(fcfsQueueService).tryEnterFcfsQueueByRedisson(result1, user1);
		BoardResponseDto result2 = boardService.findBoard(boardId, user1Id);
		assertNotNull(result2);

		// user2는 예외 발생
		given(userRepository.findById(user2Id)).willReturn(Optional.of(user2));
		doThrow(new CustomException(BoardErrorCode.EXCEED_OPEN_LIMIT))
			.when(fcfsQueueService).tryEnterFcfsQueueByRedisson(result1, user2);

		// when & then
		CustomException exception = assertThrows(CustomException.class, () ->
			boardService.findBoard(boardId, user2Id)
		);
		// assertEquals(BoardErrorCode.BOARD_NOT_YET_OPEN, exception.getBaseCode());
		assertEquals(BoardErrorCode.EXCEED_OPEN_LIMIT, exception.getBaseCode());
		verify(boardCacheService, times(2)).getOrSetCache(boardId);
	}

	@DisplayName("게시글 정상 생성(일반 게시글)")
	@Test
	public void createBoard_withValidInput_savesBoardSuccessfully() {
		// given
		Long userId = 1L;
		BoardRequestDto mockedDto = mock(BoardRequestDto.class);
		List<MultipartFile> files = List.of();
		given(mockedDto.getStoreId()).willReturn(1L);
		given(mockedDto.getType()).willReturn("N");

		NormalBoardRequestDto dto = new NormalBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "김밥천국");
		ReflectionTestUtils.setField(dto, "contents", "분식 맛도리에요~");
		ReflectionTestUtils.setField(dto, "type", "N");
		ReflectionTestUtils.setField(dto, "accessPolicy", OPEN.toString()); // 예시

		User user = UserFixture.create(ImageFixture.create());
		Store store = StoreFixture.create(CategoryFixture.create());

		Board board = BoardFixture.createNormalBoard(dto, store, user);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(storeRepository.findById(mockedDto.getStoreId())).willReturn(Optional.of(store));
		given(strategyFactory.getStrategy(eq(BoardType.N)))
			.willReturn((requestDto, s, u) -> board);
		given(boardRepository.save(any(Board.class))).willReturn(board);
		given(mockedDto.getHashtagList()).willReturn(List.of("한식"));
		// 형태소 분석
		given(processor.extractSearchKeywords(anyString())).willReturn(Set.of("김밥", "분식"));
		given(processor.extractNouns(anyString())).willReturn(List.of("김밥", "서울"));
		given(processor.extractPhrases(anyString())).willReturn(List.of("서울 김밥", "맛집 분식"));

		// when & then
		assertDoesNotThrow(() -> boardService.createBoard(userId, mockedDto, files));
		verify(strategyFactory).getStrategy(eq(BoardType.N));
		verify(hashtagService).applyHashtagsToBoard(eq(board), eq(List.of("한식")));
		verify(boardRepository).save(any(Board.class));
	}

	@DisplayName("게시글 정상 생성(오픈런 게시글)")
	@Test
	public void createBoard_withOpenRunInput_savesBoardSuccessfully() {
		// given
		Long userId = 1L;
		OpenRunBoardRequestDto mockedDto = mock(OpenRunBoardRequestDto.class);
		List<MultipartFile> files = List.of();

		given(mockedDto.getStoreId()).willReturn(1L);
		given(mockedDto.getType()).willReturn("O");
		given(mockedDto.getHashtagList()).willReturn(List.of("맛집", "한식"));

		User user = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user, "id", 1L);
		ReflectionTestUtils.setField(user, "level", Level.NORMAL);
		Store store = StoreFixture.create(CategoryFixture.create());

		// 오픈런 게시글 생성용 DTO
		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "숨겨진 김밥 맛집 선착순~");
		ReflectionTestUtils.setField(dto, "contents", "선착순 10명만!");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", FCFS.name());
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusHours(1));
		ReflectionTestUtils.setField(dto, "openLimit", 10);

		Board board = BoardFixture.createTimeAttackBoard(dto, store, user);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(storeRepository.findById(mockedDto.getStoreId())).willReturn(Optional.of(store));
		given(strategyFactory.getStrategy(eq(BoardType.O)))
			.willReturn((requestDto, s, u) -> board);
		given(boardRepository.save(any(Board.class))).willReturn(board);

		int postingLimit = user.getLevel().getPostingLimit();

		// 형태소 분석
		given(processor.extractSearchKeywords(anyString())).willReturn(Set.of("김밥", "분식"));
		given(processor.extractNouns(anyString())).willReturn(List.of("김밥", "서울"));
		given(processor.extractPhrases(anyString())).willReturn(List.of("서울 김밥", "맛집 분식"));

		// when & then
		assertDoesNotThrow(() -> boardService.createBoard(userId, mockedDto, files));

		verify(strategyFactory).getStrategy(eq(BoardType.O));
		verify(hashtagService).applyHashtagsToBoard(eq(board), eq(List.of("맛집", "한식")));
		verify(boardRepository).save(any(Board.class));
	}

	@DisplayName("게시글 생성 실패(오픈런 게시글 - 작성 가능 수 초과)")
	@Test
	void createBoard_withOpenRunInput_exceedsPostingLimit_throwsException() {
		// given
		Long userId = 1L;
		BoardRequestDto mockedDto = mock(BoardRequestDto.class);
		List<MultipartFile> files = List.of();

		given(mockedDto.getStoreId()).willReturn(1L);
		given(mockedDto.getType()).willReturn("O");
		given(mockedDto.getHashtagList()).willReturn(List.of("한식"));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "ㄹㅇ 맛집 오픈런 작성~");
		ReflectionTestUtils.setField(dto, "contents", "한정 수량!");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.toString());
		ReflectionTestUtils.setField(dto, "openLimit", 5);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));

		User user = UserFixture.create(ImageFixture.create());
		Store store = StoreFixture.create(CategoryFixture.create());
		Board board = BoardFixture.createTimeLimitedOBoard(dto, store, user);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(storeRepository.findById(mockedDto.getStoreId())).willReturn(Optional.of(store));
		given(strategyFactory.getStrategy(eq(BoardType.O))).willReturn((req, s, u) -> board);
		given(boardRepository.save(any(Board.class))).willReturn(board);
		given(processor.extractSearchKeywords(anyString())).willReturn(Set.of("오픈런", "한정"));
		given(processor.extractNouns(anyString())).willReturn(List.of("한정", "분식"));
		given(processor.extractPhrases(anyString())).willReturn(List.of("한정 수량", "오픈런 맛집"));

		// when & then
		CustomException customException = assertThrows(CustomException.class,
			() -> boardService.createBoard(userId, mockedDto, files));
		assertEquals(UserErrorCode.POSTING_COUNT_OVERFLOW, customException.getBaseCode());
	}

	@DisplayName("게시글 생성 실패(존재하지 않는 가게!)")
	@Test
	void createBoard_withInvalidStore_throwsException() {
		// given
		Long userId = 1L;
		BoardRequestDto mockedDto = mock(BoardRequestDto.class);
		List<MultipartFile> files = List.of();

		User user = UserFixture.create(ImageFixture.create());

		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(mockedDto.getStoreId()).willReturn(999L);
		given(storeRepository.findById(anyLong())).willReturn(Optional.empty());

		// when & then
		CustomException customException = assertThrows(CustomException.class,
			() -> boardService.createBoard(userId, mockedDto, files));
		assertEquals(StoreErrorCode.STORE_NOT_FOUND, customException.getBaseCode());
	}

	@DisplayName("게시글 조회 성공(일반 게시글)")
	@Test
	void findBoard_withNormalBoard_returnsBoardResponseDto() {
		// given
		Long boardId = 1L;
		Long userId = 1L;

		User user = UserFixture.create(ImageFixture.create());
		Store store = StoreFixture.create(CategoryFixture.create());

		NormalBoardRequestDto dto = new NormalBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "김밥 맛집");
		ReflectionTestUtils.setField(dto, "contents", "서울에서 제일 맛있는 김밥!");
		ReflectionTestUtils.setField(dto, "type", "N");
		ReflectionTestUtils.setField(dto, "accessPolicy", OPEN.toString());

		Board board = BoardFixture.createNormalBoard(dto, store, user);

		given(boardRepository.findActiveBoard(boardId)).willReturn(Optional.of(board));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		BoardResponseDto result = assertDoesNotThrow(() -> boardService.findBoard(boardId, userId));

		// then
		assertNotNull(result);
		assertEquals(board.getTitle(), result.getTitle());
	}

	@DisplayName("게시글 조회 성공(관리자 계정)")
	@Test
	void findBoard_asAdmin_returnsBoard() {
		Long boardId = 1L;
		Long userId = 999L;

		User user = UserFixture.create(ImageFixture.create());
		// 더미 관리자 설정
		ReflectionTestUtils.setField(user, "role", Role.ADMIN);
		Store store = StoreFixture.create(CategoryFixture.create());

		Board board = BoardFixture.createTimeLimitedOBoard(
			new OpenRunBoardRequestDto(), store, user);
		ReflectionTestUtils.setField(board, "accessPolicy", TIMEATTACK);
		ReflectionTestUtils.setField(board, "openTime", LocalDateTime.now().plusDays(1));

		BoardResponseDto responseDto = BoardResponseDto.builder().entity(board).build(); // DTO 변환 예시
		given(boardRepository.findActiveBoard(boardId)).willReturn(Optional.of(board));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(boardCacheService.getOrSetCache(boardId)).willReturn(responseDto); // 이 부분 추가

		BoardResponseDto result = boardService.findBoard(boardId, userId);
		assertNotNull(result);
		assertEquals(board.getTitle(), result.getTitle());
	}

	@DisplayName("게시글 조회 성공(오픈런 게시글 - 작성자 본인)")
	@Test
	void findBoard_asOwner_returnsBoard() {
		Long boardId = 1L;
		Long userId = 1L;

		User user = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user, "id", userId);
		Store store = StoreFixture.create(CategoryFixture.create());

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "오픈런 게시글 조회 테스트");
		ReflectionTestUtils.setField(dto, "contents", "작성자 본인 조회 테스트");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", TIMEATTACK.toString());
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
		ReflectionTestUtils.setField(dto, "openLimit", 10);

		Board board = BoardFixture.createTimeAttackBoard(dto, store, user);

		given(boardRepository.findActiveBoard(boardId)).willReturn(Optional.of(board));
		given(userRepository.findById(userId)).willReturn(Optional.of(user));
		given(boardCacheService.getOrSetCache(boardId)).willReturn(BoardResponseDto.builder().entity(board).build());

		BoardResponseDto result = boardService.findBoard(boardId, userId);

		assertNotNull(result);
		assertEquals("오픈런 게시글 조회 테스트", result.getTitle());
	}

	@DisplayName("게시글 조회 실패(존재하지 않는 게시글!)")
	@Test
	void findBoard_withInvalidBoardId_throwsException() {
		// given
		Long invalidBoardId = 999L;
		Long userId = 1L;

		User user = UserFixture.create(ImageFixture.create());
		given(boardRepository.findActiveBoard(invalidBoardId)).willReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () ->
			boardService.findBoard(invalidBoardId, userId)
		);

		assertEquals(BoardErrorCode.BOARD_NOT_FOUND, exception.getBaseCode());
	}

	@DisplayName("게시글 조회 실패(존재하지 않는 유저!)")
	@Test
	void findBoard_withInvalidUser_throwsException() {
		// given
		Long boardId = 1L;
		Long invalidUserId = 999L;
		NormalBoardRequestDto dto = new NormalBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "김밥 맛집");
		ReflectionTestUtils.setField(dto, "contents", "서울에서 제일 맛있는 김밥!");
		ReflectionTestUtils.setField(dto, "type", "N");
		ReflectionTestUtils.setField(dto, "accessPolicy", OPEN.toString());

		User user = UserFixture.create(ImageFixture.create());
		ReflectionTestUtils.setField(user, "id", invalidUserId);
		Store store = StoreFixture.create(CategoryFixture.create());
		Board board = BoardFixture.createNormalBoard(dto, store, user);

		given(boardRepository.findActiveBoard(boardId)).willReturn(Optional.of(board));
		given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

		// when & then
		CustomException exception = assertThrows(CustomException.class, () -> {
			boardService.findBoard(boardId, invalidUserId);
		});

		assertEquals(UserErrorCode.NOT_FOUND_USER, exception.getBaseCode());
	}

}

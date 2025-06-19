package com.example.taste.domain.board.service;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.repository.UserRepository;

//@ActiveProfiles("test-int")
@ActiveProfiles("test-int-docker")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BoardServiceTest {
	@LocalServerPort
	private int port;

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private BoardService boardService;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private CategoryRepository categoryRepository;

	// @Test
	// @Transactional
	// void createBoard_whenOpenRunTypeRequest_thenIncreasePostingCnt() throws IOException {
	// 	// given
	// 	Image image = ImageFixture.create();
	// 	User user = userRepository.save(UserFixture.create(image));
	// 	Category category = categoryRepository.save(CategoryFixture.create());
	// 	Store store = storeRepository.save(StoreFixture.create(category));
	//
	// 	OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
	// 	// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
	// 	ReflectionTestUtils.setField(dto, "title", "제목입니다");
	// 	ReflectionTestUtils.setField(dto, "contents", "내용입니다");
	// 	ReflectionTestUtils.setField(dto, "type", "O");
	// 	ReflectionTestUtils.setField(dto, "status", "TIMEATTACK");
	// 	ReflectionTestUtils.setField(dto, "storeId", store.getId());
	// 	ReflectionTestUtils.setField(dto, "hashtagList", List.of("맛집", "한식"));
	// 	ReflectionTestUtils.setField(dto, "openLimit", 10);
	// 	ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
	//
	// 	// when
	// 	boardService.createBoard(user.getId(), dto, new ArrayList<>());
	//
	// 	// then
	// 	assertThat(userRepository.findById(user.getId()).get().getPostingCount()).isEqualTo(1);
	// }

	// @Test
	// @Transactional
	// void createBoard_whenPostingCntIsLimit_thenThrowException() throws IOException {
	// 	// given
	// 	Image image = ImageFixture.create();
	// 	User user = userRepository.save(UserFixture.createNoMorePosting(image));
	// 	Category category = categoryRepository.save(CategoryFixture.create());
	// 	Store store = storeRepository.save(StoreFixture.create(category));
	//
	// 	OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
	// 	// BoardRequestDto-OpenRunRequestDto의 상속구조로 강제 set 필요
	// 	ReflectionTestUtils.setField(dto, "title", "제목입니다");
	// 	ReflectionTestUtils.setField(dto, "contents", "내용입니다");
	// 	ReflectionTestUtils.setField(dto, "type", "O");
	// 	ReflectionTestUtils.setField(dto, "status", "TIMEATTACK");
	// 	ReflectionTestUtils.setField(dto, "storeId", store.getId());
	// 	ReflectionTestUtils.setField(dto, "hashtagList", List.of("맛집", "한식"));
	// 	ReflectionTestUtils.setField(dto, "openLimit", 10);
	// 	ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().plusDays(1));
	//
	// 	// when, then
	// 	assertThrows(CustomException.class, () -> {
	// 		boardService.createBoard(user.getId(), dto, new ArrayList<>());
	// 	});
	// }

	// @Test
	// @Transactional
	// void tryEnterFcfsQueue_whenConvertAndSend_thenClientReceiveMsg() throws Exception {
	// 	// given
	// 	Image image = ImageFixture.create();
	// 	User user = userRepository.save(UserFixture.create(image));
	// 	Category category = categoryRepository.save(CategoryFixture.create());
	// 	Store store = storeRepository.saveAndFlush(StoreFixture.create(category));
	//
	// 	OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
	// 	ReflectionTestUtils.setField(dto, "title", "제목입니다");
	// 	ReflectionTestUtils.setField(dto, "contents", "내용입니다");
	// 	ReflectionTestUtils.setField(dto, "type", "O");
	// 	ReflectionTestUtils.setField(dto, "status", "FCFS");
	// 	ReflectionTestUtils.setField(dto, "openLimit", 1);
	// 	ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusDays(1));
	// 	Board board = boardRepository.saveAndFlush(BoardFixture.createFcfsOBoard(dto, store, user));
	//
	// 	// 연결 및 구독하고 서버에서 메시지 수신 대기 (최대 5초)
	// 	CompletableFuture<String> future = connectAndSubscribe(board.getId());
	//
	// 	// when
	// 	boardService.tryEnterFcfsQueue(board, 99L);
	// 	Thread.sleep(500);
	//
	// 	// then
	// 	String receivedMessage = future.get(5, TimeUnit.SECONDS);
	// 	assertNotNull(receivedMessage);
	// 	System.out.println("수신 메시지: " + receivedMessage);
	// }

	@Test
	void tryEnterFcfsQueue() {
		// 테스트용 docker redis 사용 -> 만약 추후에 redis를 docker에 올리게 되면 둘이 구분 가능?
		// 테스트 요소 1. openLimit 보다 zSet size가 크면 error
		// 2. 이미 zSet에 있는 유저가 다시 접근하면 데이터 삽입 안하고 기존 저장된 순위 반환
		// 3. addToZSet 수행하고 convertAndSend 수행하는지
		// 4. 동시성 문제로 인해 zSet에 삽입은 됐는데 순위가 limit을 넘어간 경우 Remove + error
	}

	@Test
	public void findBoard_whenRankExceededBoard_ThrowException() {
		// tryEnterFcfsQueue() 테스트 완료 후 진행
	}

	@Test
	public void findBoard_whenIsOpenedAndRankInFcfsBoard_thenSuccess() {
		// tryEnterFcfsQueue() 테스트 완료 후 진행
	}

	@Test
	void deleteBoard_whenFcfsBoard_thenZSetSizeIsZero() {
		// tryEnterFcfsQueue() 테스트 완료 후 진행
	}

	// TODO 유저 posting count 초기화하는 스케줄러 테스트코드 @김채진
	// TODO 타임어택 게시글 공개 시간 지나면 close 하는 스케줄러 테스트코드 @김채진

	private CompletableFuture<String> connectAndSubscribe(Long boardId) throws InterruptedException {
		// WebSocket + STOMP 통신을 위한 클라이언트 클래스
		WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());

		// JSON -> Object로 자동 변환하는 클래스
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		// WebSocket + STOMP 헤더 설정
		WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
		StompHeaders connectHeaders = new StompHeaders();
		connectHeaders.setAcceptVersion("1.2");

		// 연결 결과 수신 (future는 비동기 작업의 결과 수신)
		CompletableFuture<String> futureMessage = new CompletableFuture<>();
		CountDownLatch subscribeLatch = new CountDownLatch(1); // 구독 완료 기다리기용

		stompClient.connectAsync(
			"ws://localhost:" + port + "/ws",
			webSocketHttpHeaders,
			connectHeaders,
			new StompSessionHandlerAdapter() {
				@Override
				public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
					// 연결되면 구독 시작
					session.subscribe("/sub/openrun/board/" + boardId, new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers, Object payload) {
							// 서버에서 받은 메시지를 future에 전달하여 테스트가 종료되게 함
							futureMessage.complete((String)payload);
						}
					});
					subscribeLatch.countDown();
				}

				@Override
				public void handleTransportError(StompSession session, Throwable exception) {
					futureMessage.completeExceptionally(exception); // 연결 실패 시
				}
			}
		);

		if (!subscribeLatch.await(5, TimeUnit.SECONDS)) {
			throw new RuntimeException("STOMP 구독 완료 대기 시간 초과");
		}

		return futureMessage;
	}
}

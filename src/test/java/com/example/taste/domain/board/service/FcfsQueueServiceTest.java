package com.example.taste.domain.board.service;

import static com.example.taste.common.constant.RedisConst.*;
import static com.example.taste.common.constant.SocketConst.*;
import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.board.dto.response.BoardResponseDto;
import com.example.taste.domain.board.entity.Board;
import com.example.taste.domain.board.repository.BoardRepository;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.repository.CategoryRepository;
import com.example.taste.domain.store.repository.StoreRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.BoardFixture;
import com.example.taste.fixtures.CategoryFixture;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.StoreFixture;
import com.example.taste.fixtures.UserFixture;
import com.example.taste.property.AbstractIntegrationTest;

import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class FcfsQueueServiceTest extends AbstractIntegrationTest {
	@LocalServerPort
	private int port;

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private FcfsQueueService fcfsQueueService;
	@Autowired
	private RedisService redisService;
	@Autowired
	private RedissonClient redissonClient;

	@Tag("Performance")
	@Test
	@Transactional
	void tryEnterFcfsQueue_whenConvertAndSend_thenClientReceiveMsg() {
		// given
		Image image = ImageFixture.create();
		String pw = "password";
		String encoded = passwordEncoder.encode(pw);
		User user = userRepository.saveAndFlush(UserFixture.createWithEncodedPw(image, encoded));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.saveAndFlush(StoreFixture.create(category));
		Board board = boardRepository.saveAndFlush(
			BoardFixture.createOBoard("title", "contents", "O", FCFS.name(), 1, LocalDateTime.now().minusDays(1), store,
				user));
		String key = BOARD_SOCKET_DESTINATION + board.getId();
		BoardResponseDto dto = new BoardResponseDto(board);

		// HTTP 로그인 요청으로 세션 획득
		String json = """
			{
			  "email": "%s",
			  "password": "%s"
			}
			""".formatted(user.getEmail(), pw);

		MockHttpServletRequestBuilder loginRequest = post("/auth/signin")
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.content(json);

		try {
			MvcResult loginResult = mockMvc.perform(loginRequest)
				.andExpect(status().isOk())
				.andReturn();

			Cookie[] cookies = loginResult.getResponse().getCookies();
			String sessionId = null;
			for (Cookie cookie : cookies) {
				if ("JSESSIONID".equals(cookie.getName())) {
					sessionId = cookie.getValue();
					break;
				}
			}

			// 연결 및 구독하고 서버에서 메시지 수신 대기 (최대 5초)
			CompletableFuture<String> future = connectAndSubscribe(board.getId(), sessionId);

			// when
			fcfsQueueService.tryEnterFcfsQueueByRedisson(dto, user);

			// then
			String receivedMessage = future.get(5, TimeUnit.SECONDS);
			assertNotNull(receivedMessage);

			long remainingSlot = Math.max(0, board.getOpenLimit() - redisService.getZSetSize(key));
			assertThat(receivedMessage).contains(String.valueOf(remainingSlot));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void tryEnterFcfsQueue_whenZSetSizeOverOpenLimit_thenDeleteAndError() {
		// given
		Image image1 = ImageFixture.create();
		Image image2 = ImageFixture.create();
		User user1 = userRepository.save(UserFixture.createNoMorePosting(image1));
		User user2 = userRepository.save(UserFixture.createNoMorePosting(image2));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.save(
			BoardFixture.createOBoard("title", "contents", "O", FCFS.name(), 1,
				LocalDateTime.now(), store, user1));
		BoardResponseDto dto = new BoardResponseDto(board);

		fcfsQueueService.tryEnterFcfsQueueByRedisson(dto, user1);

		// when, then
		assertThrows(CustomException.class, () -> {
			fcfsQueueService.tryEnterFcfsQueueByRedisson(dto, user2);
		});
		String key = FCFS_KEY_PREFIX + board.getId();
		assertThat(redisService.getZSetRange(key)).isEmpty();

		// clean-up
		boardRepository.deleteById(board.getId());
		userRepository.deleteById(user1.getId());
		userRepository.deleteById(user2.getId());
		storeRepository.deleteById(store.getId());
		categoryRepository.deleteById(category.getId());
	}

	@Tag("Performance")
	@Test
	@Transactional
	void tryEnterFcfsQueue_whenFailedHasLock_thenError() {
		// given
		Image image1 = ImageFixture.create();
		User user = userRepository.save(UserFixture.createNoMorePosting(image1));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.save(
			BoardFixture.createOBoard("title", "contents", "O", FCFS.name(), 1,
				LocalDateTime.now(), store, user));
		BoardResponseDto dto = new BoardResponseDto(board);

		String lockKey = FCFS_LOCK_KEY_PREFIX + board.getId();
		RLock lock = redissonClient.getLock(lockKey);

		try {
			// 다른 스레드에서 락을 점유
			Thread lockerThread = new Thread(() -> {
				lock.lock(3, TimeUnit.SECONDS); // 3초간 점유
			});
			lockerThread.start();
			lockerThread.join(); // 스레드 정리
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// when, then
		assertThrows(CustomException.class, () -> {
			fcfsQueueService.tryEnterFcfsQueueByRedisson(dto, user);
		});
	}

	@Test
	@Transactional
	void tryEnterFcfsQueue_whenIsLocked_thenRetryAndSuccess() {
		// given
		Image image1 = ImageFixture.create();
		User user = userRepository.save(UserFixture.createNoMorePosting(image1));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.save(StoreFixture.create(category));
		Board board = boardRepository.save(
			BoardFixture.createOBoard("title", "contents", "O", FCFS.name(), 1,
				LocalDateTime.now(), store, user));
		BoardResponseDto dto = new BoardResponseDto(board);

		String lockKey = FCFS_LOCK_KEY_PREFIX + board.getId();
		RLock lock = redissonClient.getLock(lockKey);

		try {
			// 다른 스레드에서 락을 점유
			Thread lockerThread = new Thread(() -> {

				lock.lock(1, TimeUnit.SECONDS); // 1초간 점유
			});
			lockerThread.start();

			// 현재 스레드는 0.1초 대기 후 메서드 실행
			Thread.sleep(100);

			// 스레드 정리
			lockerThread.join();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// when, then
		assertDoesNotThrow(() -> fcfsQueueService.tryEnterFcfsQueueByRedisson(dto, user));
	}

	private CompletableFuture<String> connectAndSubscribe(Long boardId, String sessionId) {
		WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());

		// 세션 쿠키를 포함한 WebSocket 헤더
		WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
		webSocketHttpHeaders.add("Cookie", "JSESSIONID=" + sessionId);

		StompHeaders connectHeaders = new StompHeaders();
		connectHeaders.setAcceptVersion("1.2");

		CompletableFuture<String> futureMessage = new CompletableFuture<>();
		CountDownLatch subscribeLatch = new CountDownLatch(1);

		stompClient.connectAsync(
			"ws://localhost:" + port + "/ws",
			webSocketHttpHeaders,
			connectHeaders,
			new StompSessionHandlerAdapter() {
				@Override
				public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
					session.subscribe(BOARD_SOCKET_DESTINATION + boardId, new StompFrameHandler() {
						@Override
						public Type getPayloadType(StompHeaders headers) {
							return String.class;
						}

						@Override
						public void handleFrame(StompHeaders headers, Object payload) {
							futureMessage.complete((String)payload);
						}
					});
					subscribeLatch.countDown();
				}

				@Override
				public void handleTransportError(StompSession session, Throwable exception) {
					futureMessage.completeExceptionally(exception);
				}
			}
		);

		try {
			if (!subscribeLatch.await(5, TimeUnit.SECONDS)) {
				throw new RuntimeException("STOMP 구독 완료 대기 시간 초과");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		return futureMessage;
	}
}
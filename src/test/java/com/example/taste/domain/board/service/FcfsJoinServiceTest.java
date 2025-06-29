package com.example.taste.domain.board.service;

import static com.example.taste.domain.board.entity.AccessPolicy.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.example.taste.domain.board.dto.request.OpenRunBoardRequestDto;
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
class FcfsJoinServiceTest extends AbstractIntegrationTest {
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
	private FcfsJoinService fcfsJoinService;

	@Test
	@Transactional
	void tryEnterFcfsQueue_whenConvertAndSend_thenClientReceiveMsg() throws Exception {
		// given
		Image image = ImageFixture.create();
		String pw = "password";
		String encoded = passwordEncoder.encode(pw);
		User user = userRepository.saveAndFlush(UserFixture.createWithEncodedPw(image, encoded));
		Category category = categoryRepository.save(CategoryFixture.create());
		Store store = storeRepository.saveAndFlush(StoreFixture.create(category));

		OpenRunBoardRequestDto dto = new OpenRunBoardRequestDto();
		ReflectionTestUtils.setField(dto, "title", "제목입니다");
		ReflectionTestUtils.setField(dto, "contents", "내용입니다");
		ReflectionTestUtils.setField(dto, "type", "O");
		ReflectionTestUtils.setField(dto, "accessPolicy", FCFS.name());
		ReflectionTestUtils.setField(dto, "openLimit", 1);
		ReflectionTestUtils.setField(dto, "openTime", LocalDateTime.now().minusDays(1));
		Board board = boardRepository.saveAndFlush(BoardFixture.createFcfsOBoard(dto, store, user));

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

		MvcResult loginResult = mockMvc.perform(loginRequest)
			.andExpect(status().isOk())
			.andReturn();

		System.out.println(loginRequest);

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
		fcfsJoinService.tryEnterFcfsQueueByRedisson(board, user);
		Thread.sleep(500);

		// then
		String receivedMessage = future.get(5, TimeUnit.SECONDS);
		assertNotNull(receivedMessage);
		System.out.println("수신 메시지: " + receivedMessage);
	}

	@Test
	@Transactional
	void tryEnterFcfsQueue_zsetSizeOverOpenLimit_thenError() {
	}

	@Test
	@Transactional
	void tryEnterFcfsQueue_isLocked_thenRetryOrError() {
		// 동시성 테스트(락 점유 중일때 접근 실패 확인)
	}

	private CompletableFuture<String> connectAndSubscribe(Long boardId, String sessionId) throws Exception {
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
					session.subscribe("/sub/openrun/board/" + boardId, new StompFrameHandler() {
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

		if (!subscribeLatch.await(5, TimeUnit.SECONDS)) {
			throw new RuntimeException("STOMP 구독 완료 대기 시간 초과");
		}

		return futureMessage;
	}
}
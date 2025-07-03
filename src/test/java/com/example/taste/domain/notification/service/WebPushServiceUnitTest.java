package com.example.taste.domain.notification.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebPushServiceUnitTest {
	/*

	@Mock
	private UserRepository userRepository;
	@Mock
	private WebPushRepository webPushRepository;
	@Mock
	private ObjectMapper objectMapper;
	@Mock
	private PushService pushService;

	@InjectMocks
	private WebPushService webPushService;

	static User user;
	static Long everyId;
	static WebPushSubscription subscription;
	static String testP256dhKey = "BPuVaCJJMkp2UKXp6BvCXyDLJmMkILwEG1EjGAKPN_7Zgn54KyRk2hHV2eXjTPtIoPpB9b1c-Qi8GlXnBb9cCnQ";
	static String testAuthKey = "8r4QMPkEtcU2bVg3spLOUg";

	@BeforeAll
	static void registerBCProvider() {
		if (Security.getProvider("BC") == null) {
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	@BeforeEach
	void entities() {
		everyId = 1L;
		user = spy(UserFixture.create(null));
		ReflectionTestUtils.setField(user, "id", everyId);
		subscription = spy(WebPushSubscription.builder()
			.authKey(testAuthKey)
			.endpoint("endPoint")
			.p256dhKey(testP256dhKey)
			.user(user)
			.build());

	}

	@Nested
	class save {
		@Test
		void saveSubscription() {
			// given
			PushSubscribeRequestDto dto = new PushSubscribeRequestDto(
				"endPoint", new PushSubscribeRequestDto.Keys("p256Key", "authKey")
			);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			given(webPushRepository.findByEndpoint(anyString())).willReturn(Optional.empty());
			// when
			webPushService.saveSubscription(everyId, dto);
			// then
			then(webPushRepository).should().save(any(WebPushSubscription.class));
		}

		@Test
		void update_subscription() {
			// given
			PushSubscribeRequestDto dto = new PushSubscribeRequestDto(
				"endPoint", new PushSubscribeRequestDto.Keys("p256Key", "authKey")
			);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			given(webPushRepository.findByEndpoint(anyString())).willReturn(Optional.of(subscription));
			// when
			webPushService.saveSubscription(everyId, dto);
			// then
			then(webPushRepository).should(never()).delete(any());
			then(webPushRepository).should(never()).save(any());
		}

		@Test
		void different_user() {
			// given
			User otherUser = UserFixture.create(null);
			ReflectionTestUtils.setField(otherUser, "id", 2L);
			ReflectionTestUtils.setField(subscription, "user", otherUser);
			PushSubscribeRequestDto dto = new PushSubscribeRequestDto(
				"endPoint", new PushSubscribeRequestDto.Keys("p256Key", "authKey")
			);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			given(webPushRepository.findByEndpoint(anyString())).willReturn(Optional.of(subscription));
			// when
			webPushService.saveSubscription(everyId, dto);
			// then
			then(webPushRepository).should().delete(any(WebPushSubscription.class));
			then(webPushRepository).should().save(any(WebPushSubscription.class));
		}

	}

	@Nested
	class delete {
		@Test
		void deleteSubscription() {
			// given
			given(webPushRepository.getWebPushSubscriptionByUserIdAndEndpoint(anyLong(), anyString()))
				.willReturn(subscription);
			// when
			webPushService.deleteSubscription(everyId, "endPoint");
			// then
			then(webPushRepository).should().delete(any());
		}

		@Test
		void no_subscription() {
			// given
			given(webPushRepository.getWebPushSubscriptionByUserIdAndEndpoint(anyLong(), anyString()))
				.willReturn(null);
			// when
			webPushService.deleteSubscription(everyId, "endPoint");
			// then
			then(webPushRepository).should(never()).delete(any());
		}

	}

	@Test
	void send() throws

		IOException, GeneralSecurityException, JoseException, ExecutionException, InterruptedException {
		ReflectionTestUtils.setField(webPushService, "vapidPublic",
			"BEMtj1e85NIfBpfHRFGYYVs52YRQ7pMY0croMYWd2mI4AdAreOSWhMO9kHfFEjtT72Wnpk_sSurWoGeRMLsJfG8");
		ReflectionTestUtils.setField(webPushService, "vapidPrivate", "4ijFt-zae8xdYDkx3mk-0YK_O_PiIZ7hmY6UfMBYXjM");

		NotificationDataDto dataDto = NotificationDataDto.builder()
			.userId(everyId)
			.read(false)
			.contents("test")
			.createdAt(LocalDateTime.now())
			.redirectionUrl("testUrl")
			.category(NotificationCategory.INDIVIDUAL)
			.build();

		WebPushPayloadDto payloadDto = WebPushPayloadDto.builder()
			.category(NotificationCategory.INDIVIDUAL)
			.content("test")
			.contentId(everyId)
			.createdAt(dataDto.getCreatedAt())
			.redirectUrl("testUrl")
			.build();

		String jsonPayload = "{\"mock\":\"json\"}";

		given(subscription.getEndpoint()).willReturn("https://endpoint.com");
		given(subscription.getP256dhKey()).willReturn(testP256dhKey);
		given(subscription.getAuthKey()).willReturn(testAuthKey);

		given(objectMapper.writeValueAsString(any())).willReturn(jsonPayload);

		// PushService 체이닝 모킹
		given(pushService.setPublicKey(anyString())).willReturn(pushService);
		given(pushService.setPrivateKey(anyString())).willReturn(pushService);
		given(pushService.setSubject(anyString())).willReturn(pushService);
		when(pushService.send(any(Notification.class))).thenReturn(null);

		// Notification 가짜 객체 생성, 생성자를 가짜 생성자를 활용해 생성.
		try (MockedConstruction<Notification> mockedNotification = mockConstruction(Notification.class,
			(mock, context) -> {
				// 예시: 특정 메서드를 호출했을 때 미리 정의한 응답을 주기
				//when(mock.toString()).thenReturn("Mocked Notification!");
				//doNothing().when(mock).someVoidMethod();
				// 이런 식으로 이렇게 만든 가짜 객체에 어떤 동작을 할 때 어떤 식으로 응답할 지 정할 수 있음
				// 쓰기 어려운 객체들을 대체할 때 사용하면 좋을 듯.
			})) {

			// 실제 테스트 호출
			webPushService.send(subscription, dataDto, everyId);

			// pushService.send 호출 확인
			then(pushService).should().send(any(Notification.class));
		}
	}

	 */
}

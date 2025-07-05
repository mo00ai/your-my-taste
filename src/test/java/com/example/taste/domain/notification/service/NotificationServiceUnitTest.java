package com.example.taste.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationPublishDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.WebPushSubscription;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.entity.enums.NotificationType;
import com.example.taste.domain.notification.redis.NotificationRedisService;
import com.example.taste.domain.notification.repository.notification.NotificationContentRepository;
import com.example.taste.domain.notification.repository.notification.NotificationInfoRepository;
import com.example.taste.domain.notification.repository.webPush.WebPushRepository;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
class NotificationServiceUnitTest {

	@Mock
	private NotificationInfoRepository infoRepository;
	@Mock
	private NotificationRedisService notificationRedisService;
	@Mock
	private WebPushService webPushService;
	@Mock
	private WebPushRepository webPushRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private NotificationContentRepository notificationContentRepository;

	@InjectMocks
	private NotificationService notificationService;

	static Long everyId;
	static List<Long> everyIdList;
	static String url;
	static String content;
	static NotificationContent notificationContent;
	static NotificationDataDto dataDto;
	static User user;
	static WebPushSubscription webPushSubscription;
	static List<WebPushSubscription> webPushSubscriptions;
	static NotificationPublishDto publishDto;

	@BeforeEach
	void init() {
		everyId = 1L;
		everyIdList = new ArrayList<>();
		everyIdList.add(everyId);
		url = "testUrl";
		content = "test";

		notificationContent = NotificationContent.builder()
			.redirectionUrl(url)
			.content(content)
			.build();
		ReflectionTestUtils.setField(notificationContent, "id", everyId);

		dataDto = NotificationDataDto.builder()
			.category(NotificationCategory.INDIVIDUAL)
			.redirectionUrl(url)
			.contents(content)
			.read(false)
			.userId(1L)
			.createdAt(LocalDateTime.now())
			.build();

		user = UserFixture.create(null);
		ReflectionTestUtils.setField(user, "id", everyId);
		webPushSubscription = WebPushSubscription.builder()
			.user(user)
			.build();
		webPushSubscriptions = new ArrayList<>();
		webPushSubscriptions.add(webPushSubscription);
		publishDto = NotificationPublishDto.builder()
			.userId(everyId)
			.additionalText("text")
			.redirectionEntityId(everyId)
			.type(NotificationType.ACCEPT)
			.redirectionUrl(url)
			.build();
	}

	@Nested
	class individual {
		@Test
		void sendIndividual() {
			// given
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			given(webPushRepository.findByUserId(everyId)).willReturn(webPushSubscriptions);
			// when
			notificationService.sendIndividual(notificationContent, dataDto);
			// then
			then(webPushService).should().send(webPushSubscription, dataDto, everyId);
			then(notificationRedisService).should().storeAndTrimNotification(everyId, everyId, dataDto);
			then(infoRepository).should().save(any());
		}

		@Test
		void web_push_error() {
			// given
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			given(webPushRepository.findByUserId(everyId)).willReturn(webPushSubscriptions);
			doThrow(new RuntimeException("Push failed"))
				.when(webPushService).send(webPushSubscription, dataDto, everyId);
			// when
			notificationService.sendIndividual(notificationContent, dataDto);
			// then
			then(webPushService).should().send(webPushSubscription, dataDto, everyId);
			then(notificationRedisService).should().storeAndTrimNotification(everyId, everyId, dataDto);
			then(infoRepository).should().save(any());
		}
	}

	@Nested
	class bunch {
		@Test
		void sendBunchUsingReference() {
			// given
			everyIdList.add(everyId);
			given(webPushRepository.findByUserId(anyLong())).willReturn(webPushSubscriptions);
			// when
			notificationService.sendBunchUsingReference(notificationContent, dataDto, everyIdList);
			// then
			then(webPushService).should(times(2)).send(webPushSubscription, dataDto, everyId);
			then(notificationRedisService).should(times(2)).storeAndTrimNotification(everyId, everyId, dataDto);
			then(infoRepository).should(times(1)).saveAll(anyCollection());
		}

		@Test
		void web_push_error() {
			// given
			everyIdList.add(everyId);
			given(webPushRepository.findByUserId(everyId)).willReturn(webPushSubscriptions);
			doThrow(new RuntimeException("Push failed"))
				.when(webPushService).send(webPushSubscription, dataDto, everyId);
			// when
			notificationService.sendBunchUsingReference(notificationContent, dataDto, everyIdList);
			// then
			then(webPushService).should(times(2)).send(webPushSubscription, dataDto, everyId);
			then(notificationRedisService).should(times(2)).storeAndTrimNotification(everyId, everyId, dataDto);
			then(infoRepository).should().saveAll(anyCollection());
		}
	}

	@Nested
	class makeDto {
		@ParameterizedTest
		@MethodSource("provideCategories")
		void additional_text_only(NotificationCategory category) {
			// given
			ReflectionTestUtils.setField(publishDto, "category", category);

			// when
			NotificationDataDto result = notificationService.makeDataDto(publishDto);

			// then
			assertThat(result.getUserId()).isEqualTo(everyId);
			assertThat(result.getCategory()).isEqualTo(category);
			assertThat(result.getContents()).isEqualTo("text");
		}

		private static Stream<NotificationCategory> provideCategories() {
			return Stream.of(
				NotificationCategory.INDIVIDUAL,
				NotificationCategory.SYSTEM,
				NotificationCategory.MARKETING
			);
		}

		@Test
		void makeDataDto() {
			// given
			ReflectionTestUtils.setField(publishDto, "category", NotificationCategory.BOARD);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));
			// when
			NotificationDataDto result = notificationService.makeDataDto(publishDto);
			// then
			assertThat(result.getUserId()).isEqualTo(everyId);
			assertThat(result.getCategory()).isEqualTo(NotificationCategory.BOARD);
			assertThat(result.getContents()).isEqualTo(user.getNickname() + " 이/가"
				+ publishDto.getCategory().getCategoryText() + " 을/를"
				+ publishDto.getType().getTypeString() + " 했습니다.\n"
				+ "text"
			);
		}
	}

	@Test
	void saveContent() {
		// given
		given(notificationContentRepository.save(any())).willReturn(notificationContent);
		// when
		NotificationContent result = notificationService.saveContent(dataDto);
		// then
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getRedirectionUrl()).isEqualTo(url);
	}
}

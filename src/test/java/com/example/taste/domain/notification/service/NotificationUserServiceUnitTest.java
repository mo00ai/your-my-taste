package com.example.taste.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.image.entity.Image;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationResponseDto;
import com.example.taste.domain.notification.entity.NotificationContent;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.entity.UserNotificationSetting;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.exception.NotificationErrorCode;
import com.example.taste.domain.notification.redis.NotificationRedisService;
import com.example.taste.domain.notification.repository.notification.NotificationInfoRepository;
import com.example.taste.domain.notification.repository.notification.UserNotificationSettingRepository;
import com.example.taste.domain.user.dto.response.UserNotificationSettingResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.repository.UserRepository;
import com.example.taste.fixtures.ImageFixture;
import com.example.taste.fixtures.UserFixture;

@ExtendWith(MockitoExtension.class)
class NotificationUserServiceUnitTest {

	@Mock
	private RedisService redisService;
	@Mock
	private UserRepository userRepository;
	@Mock
	private NotificationRedisService notificationRedisService;
	@Mock
	private NotificationInfoRepository notificationInfoRepository;
	@Mock
	private UserNotificationSettingRepository userNotificationSettingRepository;

	@InjectMocks
	private NotificationUserService userService;

	@Nested
	class getCount {
		@Test
		void getNotificationCount() {
			// given
			Long everyId = 1L;

			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));

			UserNotificationSetting setting = new UserNotificationSetting(
				user, NotificationCategory.MARKETING
			);
			ReflectionTestUtils.setField(setting, "id", 1L);
			List<UserNotificationSetting> settings = new ArrayList<>();
			settings.add(setting);
			given(userNotificationSettingRepository.findAllByUser(any(User.class))).willReturn(settings);

			String key = "notification:count:user:" + everyId + ":INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			keys.add(key);
			given(redisService.getKeys(anyString())).willReturn(keys);
			given(redisService.getKeyValue(anyString())).willReturn(1L);

			// when
			GetNotificationCountResponseDto result = userService.getNotificationCount(everyId);

			// then
			then(notificationRedisService).should().deleteNotificationOfCategories(anyLong(), anyList());
			then(notificationInfoRepository).should().deleteAllByUserAndCategories(anyLong(), anyList());
			assertThat(result).isNotNull();
			assertThat(result.getNotificationCount()).isEqualTo(1);
		}

		@Test
		void no_count() {
			// given
			Long everyId = 1L;

			Image image = ImageFixture.create();
			User user = UserFixture.create(image);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));

			UserNotificationSetting setting = new UserNotificationSetting(
				user, NotificationCategory.MARKETING
			);
			ReflectionTestUtils.setField(setting, "id", 1L);
			List<UserNotificationSetting> settings = new ArrayList<>();
			settings.add(setting);
			given(userNotificationSettingRepository.findAllByUser(any(User.class))).willReturn(settings);

			String pattern = "notification:count:user:" + everyId + ":*";
			Set<String> keys = new HashSet<>();
			keys.add(pattern);
			given(redisService.getKeys(anyString())).willReturn(keys);

			// when
			GetNotificationCountResponseDto result = userService.getNotificationCount(everyId);

			// then
			then(notificationRedisService).should().deleteNotificationOfCategories(anyLong(), anyList());
			then(notificationInfoRepository).should().deleteAllByUserAndCategories(anyLong(), anyList());
			assertThat(result).isNotNull();
			assertThat(result.getNotificationCount()).isEqualTo(0);
		}
	}

	@Nested
	class getList {
		@Test
		void get_notification_list() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			String redisKey = "notification:user:1:id:1:INDIVIDUAL";
			List<String> keys = new ArrayList<>();
			keys.add(redisKey);
			given(redisService.getKeysFromList(anyString(), anyInt())).willReturn(keys);

			NotificationDataDto dataDto = new NotificationDataDto(everyId, NotificationCategory.INDIVIDUAL, "test",
				"testUrl",
				LocalDateTime.now(), false);
			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			Set<String> redisKeySet = new HashSet<>();
			redisKeySet.add(redisKey);
			given(redisService.getKeys(anyString())).willReturn(redisKeySet);

			NotificationContent content = new NotificationContent("testContent", "testContentUrl");
			NotificationInfo info = NotificationInfo.builder()
				.user(user)
				.category(NotificationCategory.INDIVIDUAL)
				.notificationContent(content)
				.build();
			ReflectionTestUtils.setField(info, "id", 1L);
			List<NotificationInfo> dbList = new ArrayList<>();
			dbList.add(info);
			Pageable pageable = PageRequest.of(0, 10);
			Slice<NotificationInfo> dbSlice = new SliceImpl<>(dbList, pageable, false);
			given(
				notificationInfoRepository.getMoreNotificationInfoWithContents(anyLong(), anyList(), any())).willReturn(
				dbSlice);

			// when
			Slice<NotificationResponseDto> result = userService.getNotificationList(everyId, 1);

			// then
			assertThat(result.getContent().get(0).getContent()).isEqualTo("test");
			assertThat(result.getContent().get(1).getContent()).isEqualTo("testContent");
		}

		@Test
		void list_full_by_redis_keys() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			List<String> keys = new ArrayList<>();
			for (int i = 1; i <= 10; i++) {
				String redisKey = "notification:user:1:id:" + i + ":INDIVIDUAL";
				keys.add(redisKey);
			}
			given(redisService.getKeysFromList(anyString(), anyInt())).willReturn(keys);

			NotificationDataDto dataDto = new NotificationDataDto(everyId, NotificationCategory.INDIVIDUAL, "test",
				"testUrl",
				LocalDateTime.now(), false);
			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			// when
			Slice<NotificationResponseDto> result = userService.getNotificationList(everyId, 1);

			// then
			for (int i = 1; i <= 10; i++) {
				assertThat(result.getContent().get(i - 1).getContentId()).isEqualTo(i);
			}
		}

		@Test
		void redis_list_bigger_then_10() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			List<String> keys = new ArrayList<>();
			for (int i = 1; i <= 13; i++) {
				String redisKey = "notification:user:1:id:" + i + ":INDIVIDUAL";
				keys.add(redisKey);
			}
			given(redisService.getKeysFromList(anyString(), anyInt())).willReturn(keys);

			NotificationDataDto dataDto = new NotificationDataDto(everyId, NotificationCategory.INDIVIDUAL, "test",
				"testUrl",
				LocalDateTime.now(), false);
			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			// when
			Slice<NotificationResponseDto> result = userService.getNotificationList(everyId, 1);

			// then
			for (int i = 1; i <= 10; i++) {
				assertThat(result.getContent().get(i - 1).getContentId()).isEqualTo(i);
			}
		}

		@Test
		void no_redis_key() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			List<String> keys = new ArrayList<>();
			given(redisService.getKeysFromList(anyString(), anyInt())).willReturn(keys);

			NotificationContent content = new NotificationContent("testContent", "testContentUrl");
			NotificationInfo info = NotificationInfo.builder()
				.user(user)
				.category(NotificationCategory.INDIVIDUAL)
				.notificationContent(content)
				.build();
			ReflectionTestUtils.setField(info, "id", 1L);
			List<NotificationInfo> dbList = new ArrayList<>();
			dbList.add(info);
			Pageable pageable = PageRequest.of(0, 10);
			Slice<NotificationInfo> dbSlice = new SliceImpl<>(dbList, pageable, false);
			given(
				notificationInfoRepository.getMoreNotificationInfoWithContents(anyLong(), anyList(), any())).willReturn(
				dbSlice);

			// when
			Slice<NotificationResponseDto> result = userService.getNotificationList(everyId, 1);

			// then
			assertThat(result.getContent().get(0).getContent()).isEqualTo("testContent");
		}
	}

	@Nested
	class mark {
		@Test
		void markNotificationAsRead() {
			// given
			Long everyId = 1L;

			String key = "notification:user:" + everyId + ":id:" + everyId + ":INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			keys.add(key);
			given(redisService.getKeys(anyString())).willReturn(keys);

			NotificationDataDto dataDto = NotificationDataDto.builder()
				.redirectionUrl("testUrl")
				.category(NotificationCategory.INDIVIDUAL)
				.createdAt(LocalDateTime.now())
				.contents("test")
				.read(false)
				.userId(everyId)
				.build();

			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			User user = UserFixture.create(null);
			NotificationContent content = NotificationContent.builder().build();
			ReflectionTestUtils.setField(content, "id", 1L);
			NotificationInfo info = NotificationInfo.builder()
				.notificationContent(content)
				.category(NotificationCategory.INDIVIDUAL)
				.user(user)
				.build();
			List<NotificationInfo> infos = new ArrayList<>();
			infos.add(info);
			given(notificationInfoRepository.getNotificationInfoWithContents(anyLong(), anyList())).willReturn(infos);

			// when
			userService.markNotificationAsRead(everyId, everyId);

			// then
			then(redisService).should().decreaseCount(anyString());
			then(notificationRedisService).should().updateNotification(any(), anyString());
			then(notificationInfoRepository).should().saveAll(anyCollection());
		}

		@Test
		void already_read() {
			// given
			Long everyId = 1L;

			String key = "notification:user:" + everyId + ":id:" + everyId + "INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			keys.add(key);
			given(redisService.getKeys(anyString())).willReturn(keys);

			NotificationDataDto dataDto = NotificationDataDto.builder()
				.redirectionUrl("testUrl")
				.category(NotificationCategory.INDIVIDUAL)
				.createdAt(LocalDateTime.now())
				.contents("test")
				.read(true)
				.userId(everyId)
				.build();

			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			User user = UserFixture.create(null);
			NotificationContent content = NotificationContent.builder().build();
			ReflectionTestUtils.setField(content, "id", 1L);
			NotificationInfo info = NotificationInfo.builder()
				.notificationContent(content)
				.category(NotificationCategory.INDIVIDUAL)
				.user(user)
				.build();
			List<NotificationInfo> infos = new ArrayList<>();
			infos.add(info);
			given(notificationInfoRepository.getNotificationInfoWithContents(anyLong(), anyList())).willReturn(infos);

			// when
			userService.markNotificationAsRead(everyId, everyId);

			// then
			then(notificationRedisService).should().updateNotification(any(), anyString());
			then(notificationInfoRepository).should().saveAll(anyCollection());
		}

		@Test
		void no_key() {
			// given
			Long everyId = 1L;

			String key = "notification:user:" + everyId + ":id:" + everyId + "INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			given(redisService.getKeys(anyString())).willReturn(keys);

			// when
			userService.markNotificationAsRead(everyId, everyId);

			// then
			then(notificationInfoRepository).should().saveAll(anyCollection());
		}

		@Test
		void bad_value() {
			// given
			Long everyId = 1L;

			String key = "notification:user:" + everyId + ":id:" + everyId + "INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			keys.add(key);
			given(redisService.getKeys(anyString())).willReturn(keys);

			given(redisService.getKeyValue(any())).willReturn("bad value");

			// when & then
			assertThatThrownBy(() -> userService.markNotificationAsRead(everyId, everyId))
				.isInstanceOf(CustomException.class)
				.hasMessageContaining(NotificationErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
		}
	}

	@Nested
	class markAll {
		@Test
		void markAllNotificationAsRead() {
			// given
			Long everyId = 1L;

			String key = "notification:user:1:id:189:INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			keys.add(key);
			given(redisService.getKeys(anyString())).willReturn(keys);

			NotificationDataDto dataDto = NotificationDataDto.builder()
				.redirectionUrl("testUrl")
				.category(NotificationCategory.INDIVIDUAL)
				.createdAt(LocalDateTime.now())
				.contents("test")
				.read(false)
				.userId(everyId)
				.build();
			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			// when
			userService.markAllNotificationAsRead(everyId);

			// then
			then(redisService).should().decreaseCount(anyString());
			then(notificationRedisService).should().updateNotification(dataDto, key);
			then(notificationInfoRepository).should().getNotificationInfoWithContents(anyLong(), anyList());
			then(notificationInfoRepository).should().saveAll(anyCollection());
		}

		@Test
		void already_read() {
			// given
			Long everyId = 1L;

			String key = "notification:user:1:id:189:INDIVIDUAL";
			Set<String> keys = new HashSet<>();
			keys.add(key);
			given(redisService.getKeys(anyString())).willReturn(keys);

			NotificationDataDto dataDto = NotificationDataDto.builder()
				.redirectionUrl("testUrl")
				.category(NotificationCategory.INDIVIDUAL)
				.createdAt(LocalDateTime.now())
				.contents("test")
				.read(true)
				.userId(everyId)
				.build();
			given(redisService.getKeyValue(anyString())).willReturn(dataDto);

			// when
			userService.markAllNotificationAsRead(everyId);

			// then
			then(notificationRedisService).should().updateNotification(dataDto, key);
			then(notificationInfoRepository).should().getNotificationInfoWithContents(anyLong(), anyList());
			then(notificationInfoRepository).should().saveAll(anyCollection());
		}

		@Test
		void redis_key_empty() {
			// given
			Long everyId = 1L;

			Set<String> keys = new HashSet<>();
			given(redisService.getKeys(anyString())).willReturn(keys);

			// when
			userService.markAllNotificationAsRead(everyId);

			// then
			then(notificationInfoRepository).should().getNotificationInfoWithContents(anyLong(), anyList());
			then(notificationInfoRepository).should().saveAll(anyCollection());
		}
	}

	@Nested
	class setting {
		@Test
		void userNotificationSetting() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			ReflectionTestUtils.setField(user, "id", everyId);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));

			Optional<UserNotificationSetting> maybeSetting = Optional.empty();
			given(userNotificationSettingRepository.findByUserAndNotificationCategory(
				user, NotificationCategory.INDIVIDUAL))
				.willReturn(maybeSetting);

			// when
			UserNotificationSettingResponseDto result = userService.userNotificationSetting(
				NotificationCategory.INDIVIDUAL, true, everyId);
			// then
			then(userNotificationSettingRepository).should().save(any(UserNotificationSetting.class));
			assertThat(result.getNotificationCategory()).isEqualTo(NotificationCategory.INDIVIDUAL);
			assertThat(result.getUserId()).isEqualTo(everyId);
			assertThat(result.isSet()).isTrue();
		}

		@Test
		void already_set() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			ReflectionTestUtils.setField(user, "id", everyId);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));

			UserNotificationSetting setting = UserNotificationSetting.builder()
				.user(user)
				.notificationCategory(NotificationCategory.INDIVIDUAL)
				.build();
			Optional<UserNotificationSetting> maybeSetting = Optional.of(setting);
			given(userNotificationSettingRepository.findByUserAndNotificationCategory(user,
				NotificationCategory.INDIVIDUAL))
				.willReturn(maybeSetting);

			// when
			UserNotificationSettingResponseDto result = userService.userNotificationSetting(
				NotificationCategory.INDIVIDUAL, true, everyId);
			// then
			assertThat(result.getNotificationCategory()).isEqualTo(NotificationCategory.INDIVIDUAL);
			assertThat(result.getUserId()).isEqualTo(everyId);
			assertThat(result.isSet()).isTrue();
		}

		@Test
		void unset() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			ReflectionTestUtils.setField(user, "id", everyId);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));

			UserNotificationSetting setting = UserNotificationSetting.builder()
				.user(user)
				.notificationCategory(NotificationCategory.INDIVIDUAL)
				.build();
			Optional<UserNotificationSetting> maybeSetting = Optional.of(setting);
			given(userNotificationSettingRepository
				.findByUserAndNotificationCategory(user, NotificationCategory.INDIVIDUAL))
				.willReturn(maybeSetting);

			// when
			UserNotificationSettingResponseDto result = userService.userNotificationSetting(
				NotificationCategory.INDIVIDUAL, false, everyId);
			// then
			assertThat(result.getNotificationCategory()).isEqualTo(NotificationCategory.INDIVIDUAL);
			assertThat(result.getUserId()).isEqualTo(everyId);
			assertThat(result.isSet()).isFalse();
		}

		@Test
		void already_unset() {
			// given
			Long everyId = 1L;
			User user = UserFixture.create(null);
			ReflectionTestUtils.setField(user, "id", everyId);
			given(userRepository.findById(everyId)).willReturn(Optional.of(user));

			Optional<UserNotificationSetting> maybeSetting = Optional.empty();
			given(userNotificationSettingRepository
				.findByUserAndNotificationCategory(user, NotificationCategory.INDIVIDUAL))
				.willReturn(maybeSetting);

			// when
			UserNotificationSettingResponseDto result = userService.userNotificationSetting(
				NotificationCategory.INDIVIDUAL, false, everyId);
			// then
			assertThat(result.getNotificationCategory()).isEqualTo(NotificationCategory.INDIVIDUAL);
			assertThat(result.getUserId()).isEqualTo(everyId);
			assertThat(result.isSet()).isFalse();
		}
	}
}

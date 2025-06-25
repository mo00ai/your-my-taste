package com.example.taste.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationResponseDto;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.entity.UserNotificationSetting;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.example.taste.domain.notification.exception.NotificationErrorCode;
import com.example.taste.domain.notification.redis.NotificationRedisService;
import com.example.taste.domain.notification.repository.notification.NotificationInfoRepository;
import com.example.taste.domain.notification.repository.notification.UserNotificationSettingRepository;
import com.example.taste.domain.user.dto.response.UserNotificationSettingResponseDto;
import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.exception.UserErrorCode;
import com.example.taste.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationUserService {
	private final RedisService redisService;
	private final UserRepository userRepository;
	private final NotificationRedisService notificationRedisService;
	private final NotificationInfoRepository notificationInfoRepository;
	private final UserNotificationSettingRepository userNotificationSettingRepository;

	// 알림 개수 조회
	public GetNotificationCountResponseDto getNotificationCount(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.NOT_FOUND_USER));
		List<UserNotificationSetting> settings = userNotificationSettingRepository.findAllByUser(user);
		List<NotificationCategory> categories = new ArrayList<>();
		for (UserNotificationSetting setting : settings) {
			categories.add(setting.getNotificationCategory());
		}

		notificationRedisService.deleteNotificationOfCategorys(userId, categories);
		notificationInfoRepository.deleteAllByUserAndCategories(userId, categories);
		String pattern = "notification:count:user:" + userId + ":*";
		Set<String> keys = redisService.getKeys(pattern);
		Long count = 0L;
		count += getCount(keys);
		return new GetNotificationCountResponseDto(count);
	}

	private Long getCount(Set<String> keys) {
		Long count = 0L;
		for (String key : keys) {
			Object value = redisService.getKeyValue(key);
			if (value instanceof Number) {
				count += (long)value;
			}
		}
		return count;
	}

	//최근 알림 조회(redis)
	public Slice<NotificationResponseDto> getNotificationList(Long userId,
		int index) {
		// userId를 이용해 저장된 모든 알림을 가져옴
		String listKey = "notification:list:user:" + userId;
		List<String> keys = redisService.getKeysFromList(listKey, index - 1);
		List<NotificationResponseDto> responseDtoList = new ArrayList<>();
		boolean hasNext = keys.size() > 10;
		if (hasNext) {
			keys = keys.subList(0, 10);
		}
		for (String key : keys) {
			String[] splitKey = key.split(":");
			Long contentId = Long.parseLong(splitKey[splitKey.length - 2]);
			NotificationDataDto notificationDataDto = (NotificationDataDto)redisService.getKeyValue(key);
			NotificationResponseDto responseDto = new NotificationResponseDto(notificationDataDto);
			responseDto.setContentId(contentId);
			responseDtoList.add(responseDto);
		}
		Pageable pageable = PageRequest.of(index - 1, 10);
		return new SliceImpl<>(responseDtoList, pageable, hasNext);
	}

	// 오래된 알림 조회(mysql)
	@Transactional(readOnly = true)
	public Slice<NotificationResponseDto> getMoreNotificationList(Long userId,
		int index) {
		//redis가 가지고 있는 유저의 모든 알림을 조회
		String pattern = "notification:user:" + userId + "*";
		Set<String> keys = getKeys(pattern);

		//가져온 리스트에서 contentsId만 조회
		List<Long> redisContents = new ArrayList<>();
		if (!keys.isEmpty()) {
			for (String key : keys) {
				redisContents.add(extractContentIdFromKey(key));
			}
		}
		//해당 contentsId 를 가지지 않은 모든 sql 알림을 조회.
		Pageable pageable = PageRequest.of(index - 1, 10, Sort.by("createdAt"));
		Slice<NotificationInfo> notificationInfos = notificationInfoRepository.getMoreNotificationInfoWithContents(
			userId, redisContents, pageable);

		return notificationInfos.map(NotificationResponseDto::new);
	}

	// 알림 읽음 처리
	// 유저가 알림을 클릭하면 호출
	@Transactional
	public void markNotificationAsRead(Long userId, Long contentID) {
		// redis 알림 읽음 처리
		String pattern = "notification:user:" + userId + ":id:" + contentID + "*";
		Set<String> keys = getKeys(pattern);
		if (!keys.isEmpty()) {
			String key = keys.iterator().next();
			NotificationDataDto dto = getNotificationOrThrow(key);

			if (!dto.isRead()) {
				String countKey = "notification:count:user:" + userId + ":" + dto.getCategory().name();
				redisService.decreaseCount(countKey);
			}
			dto.readIt();
			notificationRedisService.updateNotification(dto, key);
		}

		// mysql 알림 읽음 처리
		List<Long> list = new ArrayList<>();
		list.add(contentID);
		markSqlNotificationAsRead(userId, list);
	}

	// 모든 알림 읽음 처리
	public void markAllNotificationAsRead(Long userId) {
		String pattern = "notification:user:" + userId + "*";
		Set<String> keys = getKeys(pattern);

		// redis 알림 읽음 처리
		if (!keys.isEmpty()) {
			for (String key : keys) {
				NotificationDataDto dto = getNotificationOrThrow(key);
				if (!dto.isRead()) {
					String countKey = "notification:count:user:" + userId + ":" + dto.getCategory().name();
					redisService.decreaseCount(countKey);
				}
				dto.readIt();
				notificationRedisService.updateNotification(dto, key);
			}
		} else {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}

		// mysql 알림 읽음 처리
		List<Long> emptyList = new ArrayList<>();
		markSqlNotificationAsRead(userId, emptyList);
	}

	@Transactional
	public UserNotificationSettingResponseDto userNotificationSetting(
		NotificationCategory category,
		boolean isSet,
		User user
	) {
		Optional<UserNotificationSetting> settingOpt =
			userNotificationSettingRepository.findByUserAndNotificationCategory(user, category);

		if (!isSet && settingOpt.isPresent()) {
			userNotificationSettingRepository.delete(settingOpt.get());
			return new UserNotificationSettingResponseDto(user.getId(), user.getNickname(), category, isSet);
		}

		if (isSet && settingOpt.isEmpty()) {
			UserNotificationSetting newSetting = UserNotificationSetting.builder()
				.notificationCategory(category)
				.user(user)
				.build();
			userNotificationSettingRepository.save(newSetting);
		}

		return new UserNotificationSettingResponseDto(user.getId(), user.getNickname(), category, isSet);
	}

	// 읽음 처리 중복 코드 정리

	private Set<String> getKeys(String pattern) {
		return redisService.getKeys(pattern);
	}

	private NotificationDataDto getNotificationOrThrow(String key) {
		Object obj = redisService.getKeyValue(key);
		if (obj instanceof NotificationDataDto dataDto) {
			return dataDto;
		}
		throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
	}

	private Long extractContentIdFromKey(String key) {
		String[] parts = key.split(":");
		return Long.parseLong(parts[parts.length - 2]);
	}

	private void markSqlNotificationAsRead(Long userId, List<Long> contentsIds) {
		List<NotificationInfo> mysqlNotifications = notificationInfoRepository.getNotificationInfoWithContents(userId,
			contentsIds);
		mysqlNotifications.forEach(NotificationInfo::readIt);
		notificationInfoRepository.saveAll(mysqlNotifications);
	}
}

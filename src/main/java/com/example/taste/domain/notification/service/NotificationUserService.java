package com.example.taste.domain.notification.service;

import java.util.ArrayList;
import java.util.List;
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
import com.example.taste.domain.user.entity.CustomUserDetails;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationDataDto;
import com.example.taste.domain.notification.dto.NotificationResponseDto;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.exception.NotificationErrorCode;
import com.example.taste.domain.notification.repository.NotificationInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationUserService {
	private final RedisService redisService;
	private final NotificationInfoRepository notificationInfoRepository;

	// 알림 개수 조회
	public GetNotificationCountResponseDto getNotificationCount(CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		String system = "notification:count:user:" + userId + ":" + NotificationCategory.SYSTEM;
		String marketing = "notification:count:user:" + userId + ":" + NotificationCategory.MARKETING;
		String subscribers = "notification:count:user:" + userId + ":" + NotificationCategory.SUBSCRIBE;
		// TODO 유저가 원하지 않는 카테고리 알림을 여기서 삭제.
		Long count = 0L;
		count += getCount(system);
		count += getCount(marketing);
		count += getCount(subscribers);

		return new GetNotificationCountResponseDto(count);
	}

	private Long getCount(String key) {
		Object value = redisService.getKeyValue(key);
		return (value instanceof Number) ? ((Number)value).longValue() : 0L;
	}

	//최근 알림 조회(redis)
	public Slice<NotificationResponseDto> getNotificationList(CustomUserDetails userDetails,
		int index) {
		Long userId = userDetails.getId();
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
	public Slice<NotificationResponseDto> getMoreNotificationList(CustomUserDetails userDetails,
		int index) {
		Long userId = userDetails.getId();
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
	public void markNotificationAsRead(CustomUserDetails userDetails, Long contentID) {
		Long userId = userDetails.getId();
		// redis 알림 읽음 처리
		String pattern = "notification:user:" + userId + ":id:" + contentID + "*";
		Set<String> keys = getKeys(pattern);
		if (!keys.isEmpty()) {
			String key = keys.iterator().next();
			NotificationDataDto dto = getNotificationOrThrow(key);

			if (!dto.isRead()) {
				String countKey = "notification:count:user:" + userId + ":" + dto.getCategory().name();
				redisService.decreaseCount(countKey, 1L);
			}
			dto.readIt();
			redisService.updateNotification(dto, key);
		}

		// mysql 알림 읽음 처리
		List<Long> list = new ArrayList<>();
		list.add(contentID);
		markSqlNotificationAsRead(userId, list);
	}

	// 모든 알림 읽음 처리
	public void markAllNotificationAsRead(CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		String pattern = "notification:user:" + userId + "*";
		Set<String> keys = getKeys(pattern);

		// redis 알림 읽음 처리
		if (!keys.isEmpty()) {
			for (String key : keys) {
				NotificationDataDto dto = getNotificationOrThrow(key);
				if (!dto.isRead()) {
					String countKey = "notification:count:user:" + userId + ":" + dto.getCategory().name();
					redisService.decreaseCount(countKey, 1L);
				}
				dto.readIt();
				redisService.updateNotification(dto, key);
			}
		} else {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}

		// mysql 알림 읽음 처리
		List<Long> emptyList = new ArrayList<>();
		markSqlNotificationAsRead(userId, emptyList);
	}

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

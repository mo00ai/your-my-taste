package com.example.taste.domain.notification.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationDto;
import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.exception.NotificationErrorCode;
import com.example.taste.domain.notification.repository.NotificationInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationUserService {
	private final RedisService redisService;
	private final NotificationInfoRepository notificationInfoRepository;

	public GetNotificationCountResponseDto getNotificationCount(CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		String system = "notification:user:" + userId + ":" + NotificationCategory.SYSTEM + ":unreadCount";
		String marketing = "notification:user:" + userId + ":" + NotificationCategory.MARKETING + ":unreadCount";
		String subscribers = "notification:user:" + userId + ":" + NotificationCategory.SUBSCRIBERS + ":unreadCount";
		// TODO 유저가 원하지 않는 카테고리 알림을 여기서 삭제.
		Long count = 0L;
		count += getCount(system);
		count += getCount(marketing);
		count += getCount(subscribers);

		return new GetNotificationCountResponseDto(count);
	}

	private Long getCount(String key) {
		Object value = redisService.getKeyValue(key);
		return (value != null) ? (Long)value : 0L;
	}

	public Slice<NotificationDto> getNotificationList(CustomUserDetails userDetails,
		int index) {
		Long userId = userDetails.getId();
		String pattern = "notification:info:user:" + userId;
		Set<String> keys = getKeys(pattern);
		if (keys.isEmpty()) {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}

		List<NotificationDto> notifications = new ArrayList<>();
		for (String key : keys) {
			notifications.add(getNotificationOrThrow(key));
		}
		int pageSize = 10;
		int here = index * pageSize;
		int there = Math.min(here + pageSize + 1, notifications.size());

		if (here >= notifications.size()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.of(index, pageSize), false);
		}
		Pageable pageable = PageRequest.of(index, pageSize);

		List<NotificationDto> sub = notifications.subList(here, there).stream()
			.sorted(Comparator.comparing(NotificationDto::getCreatedAt).reversed())
			.collect(Collectors.toList());
		Boolean hasNext = sub.size() > pageSize;

		if (hasNext) {
			sub = sub.subList(0, pageSize);
		}

		List<Long> idList = sub.stream().map(NotificationDto::getContentId).collect(Collectors.toList());
		markSqlNotificationAsRead(userId, idList);
		return new SliceImpl<>(sub, pageable, hasNext);
	}

	public Slice<NotificationDto> getMoreNotificationList(CustomUserDetails userDetails,
		int index) {
		Long userId = userDetails.getId();
		String pattern = "notification:info:user:" + userId;
		Set<String> keys = getKeys(pattern);
		List<Long> redisContents = new ArrayList<>();
		for (String key : keys) {
			redisContents.add(extractContentIdFromKey(key));
		}
		Pageable pageable = PageRequest.of(index, 10, Sort.by("createdAt"));
		Slice<NotificationInfo> notificationInfos = notificationInfoRepository.getMoreNotificationInfoWithContents(
			userId, redisContents, pageable);

		notificationInfos.forEach(notificationInfo -> {
			if (!notificationInfo.getIsRead()) {
				String key = "notification:count:user:" + userId + ":" + notificationInfo.getCategory().name();
				redisService.decreaseCount(key, 1L);
				notificationInfo.readIt();
			}
		});
		notificationInfoRepository.saveAll(notificationInfos.getContent());

		return notificationInfos.map(NotificationDto::new);
	}

	public void markNotificationAsRead(CustomUserDetails userDetails, Long uuid) {
		Long userId = userDetails.getId();
		String pattern = "notification:info:user:" + userId + ":id:" + uuid;
		Set<String> keys = getKeys(pattern);
		String key = keys.iterator().next();
		NotificationDto dto = getNotificationOrThrow(key);

		if (!dto.isRead()) {
			String countKey = "notification:count:user:" + userId + ":" + dto.getCategory().name();
			redisService.decreaseCount(countKey, 1L);
		}
		dto.readIt();
		redisService.updateNotification(dto, key);
		List<Long> list = new ArrayList<>();
		list.add(dto.getContentId());
		markSqlNotificationAsRead(userId, list);
	}

	public void markAllNotificationAsRead(CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		String pattern = "notification:info:user:" + userId;
		Set<String> keys = getKeys(pattern);
		if (keys.isEmpty()) {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}
		List<Long> redisContents = new ArrayList<>();
		for (String key : keys) {
			NotificationDto dto = getNotificationOrThrow(key);
			dto.readIt();
			redisService.updateNotification(dto, key);
			redisContents.add(dto.getContentId());
		}
		markSqlNotificationAsRead(userId, redisContents);
	}

	private Set<String> getKeys(String pattern) {
		Set<String> keys = redisService.getKeys(pattern);
		if (keys.isEmpty()) {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}
		return keys;
	}

	private NotificationDto getNotificationOrThrow(String key) {
		Object obj = redisService.getKeyValue(key);
		if (obj instanceof NotificationDto notification) {
			return notification;
		}
		throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
	}

	private Long extractContentIdFromKey(String key) {
		String[] parts = key.split(":");
		return Long.parseLong(parts[parts.length - 2]);
	}

	private void markSqlNotificationAsRead(Long userId, List<Long> readNotificationIds) {
		List<NotificationInfo> mysqlNotifications = notificationInfoRepository.getNotificationInfoWithContents(userId,
			readNotificationIds);
		mysqlNotifications.forEach(NotificationInfo::readIt);
		notificationInfoRepository.saveAll(mysqlNotifications);
	}

}

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
import org.springframework.transaction.annotation.Transactional;

import com.example.taste.common.exception.CustomException;
import com.example.taste.common.service.RedisService;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.dto.GetNotificationCountResponseDto;
import com.example.taste.domain.notification.dto.NotificationEventDto;
import com.example.taste.domain.notification.dto.NotificationResponseDto;
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
		String system = "notification:count:user:" + userId + ":" + NotificationCategory.SYSTEM;
		String marketing = "notification:count:user:" + userId + ":" + NotificationCategory.MARKETING;
		String subscribers = "notification:count:user:" + userId + ":" + NotificationCategory.SUBSCRIBERS;
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

	//TODO sortedSet
	public Slice<NotificationResponseDto> getNotificationList(CustomUserDetails userDetails,
		int index) {
		Long userId = userDetails.getId();
		String pattern = "notification:user:" + userId;
		Set<String> keys = getKeys(pattern);
		if (keys.isEmpty()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.of(index, 0), false);
		}

		List<NotificationEventDto> notifications = new ArrayList<>();
		for (String key : keys) {
			notifications.add(getNotificationOrThrow(key));
		}

		List<NotificationEventDto> sorted = notifications.stream()
			.sorted(Comparator.comparing(NotificationEventDto::getCreatedAt).reversed())
			.toList();

		int pageSize = 10;
		int here = index * pageSize;
		int there = Math.min(here + pageSize + 1, sorted.size());

		if (here >= sorted.size()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.of(index, pageSize), false);
		}
		Pageable pageable = PageRequest.of(index, pageSize);

		List<NotificationEventDto> sub = sorted.subList(here, there);
		Boolean hasNext = sub.size() > pageSize;

		if (hasNext) {
			sub = sub.subList(0, pageSize);
		}

		List<Long> idList = sub.stream().map(NotificationEventDto::getContentId).collect(Collectors.toList());
		markSqlNotificationAsRead(userId, idList);
		Slice<NotificationEventDto> slice = new SliceImpl<>(sub, pageable, hasNext);
		return slice.map(NotificationResponseDto::new);
	}

	public Slice<NotificationResponseDto> getMoreNotificationList(CustomUserDetails userDetails,
		int index) {
		Long userId = userDetails.getId();
		String pattern = "notification:user:" + userId;
		Set<String> keys = getKeys(pattern);
		if (keys.isEmpty()) {
			return new SliceImpl<>(Collections.emptyList(), PageRequest.of(index, 0), false);
		}
		List<Long> redisContents = new ArrayList<>();
		for (String key : keys) {
			redisContents.add(extractContentIdFromKey(key));
		}
		Pageable pageable = PageRequest.of(index, 10, Sort.by("createdAt"));
		Slice<NotificationInfo> notificationInfos = notificationInfoRepository.getMoreNotificationInfoWithContents(
			userId, redisContents, pageable);
		notificationInfoRepository.saveAll(notificationInfos.getContent());

		return notificationInfos.map(NotificationResponseDto::new);
	}

	@Transactional
	public void markNotificationAsRead(CustomUserDetails userDetails, Long uuid) {
		Long userId = userDetails.getId();
		String pattern = "notification:user:" + userId + ":id:" + uuid;
		Set<String> keys = getKeys(pattern);
		String key = keys.iterator().next();
		NotificationEventDto dto = getNotificationOrThrow(key);

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
		String pattern = "notification:user:" + userId;
		Set<String> keys = getKeys(pattern);
		if (keys.isEmpty()) {
			throw new CustomException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
		}
		List<Long> redisContents = new ArrayList<>();
		for (String key : keys) {
			NotificationEventDto dto = getNotificationOrThrow(key);
			dto.readIt();
			redisService.updateNotification(dto, key);
			redisContents.add(dto.getContentId());
		}
		markSqlNotificationAsRead(userId, redisContents);
	}

	private Set<String> getKeys(String pattern) {
		return redisService.getKeys(pattern);
	}

	private NotificationEventDto getNotificationOrThrow(String key) {
		Object obj = redisService.getKeyValue(key);
		if (obj instanceof NotificationEventDto eventDto) {
			return eventDto;
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

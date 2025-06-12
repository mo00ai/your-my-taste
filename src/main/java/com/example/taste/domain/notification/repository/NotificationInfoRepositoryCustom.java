package com.example.taste.domain.notification.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.taste.domain.notification.entity.NotificationInfo;

public interface NotificationInfoRepositoryCustom {
	Slice<NotificationInfo> getMoreNotificationInfoWithContents(Long userId, List<Long> redisNotifications,
		Pageable pageable);

	List<NotificationInfo> getNotificationInfoWithContents(Long userId, List<Long> contentsIds);
}

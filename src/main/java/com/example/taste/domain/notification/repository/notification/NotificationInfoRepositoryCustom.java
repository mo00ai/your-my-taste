package com.example.taste.domain.notification.repository.notification;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.example.taste.domain.notification.entity.NotificationInfo;
import com.example.taste.domain.notification.entity.enums.NotificationCategory;

public interface NotificationInfoRepositoryCustom {
	Slice<NotificationInfo> getMoreNotificationInfoWithContents(Long userId, List<Long> redisNotifications,
		Pageable pageable);

	List<NotificationInfo> getNotificationInfoWithContents(Long userId, List<Long> contentsIds);

	void deleteAllByUserAndCategories(Long userId, List<NotificationCategory> categories);
}

package com.example.taste.domain.notification.redis;

import java.util.Set;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;

public interface CategorySupport {
	Set<NotificationCategory> getSupportedCategories();
}

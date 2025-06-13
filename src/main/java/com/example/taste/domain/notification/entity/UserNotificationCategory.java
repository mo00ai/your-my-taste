package com.example.taste.domain.notification.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserNotificationCategory {
	private Long userId;
	private String category;
}

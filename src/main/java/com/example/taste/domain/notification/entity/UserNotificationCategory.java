package com.example.taste.domain.notification.entity;

import java.io.Serializable;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserNotificationCategory implements Serializable {
	private Long userId;
	private String category;
}

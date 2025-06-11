package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
	private NotificationCategory category;
	private String contents;
	private String redirectUrl;
	private Long targetUserId;
}

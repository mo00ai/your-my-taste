package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
	private NotificationType notificationType;

	private NotificationCategory category;
	private String content;
	private String redirectUrl;

	// 유저 타겟 알림의 경우 알림을 보낼 유저
	// 구독자 위한 알림인 경우 구독 받은 유저(이 유저의 구독자들을 찾아서 알림을 보냄)
	private Long userId;
}

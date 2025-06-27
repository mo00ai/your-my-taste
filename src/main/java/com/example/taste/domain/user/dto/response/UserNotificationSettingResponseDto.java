package com.example.taste.domain.user.dto.response;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationSettingResponseDto {
	private Long userId;
	private String userNickname;
	private NotificationCategory notificationCategory;
	private boolean isSet;
}

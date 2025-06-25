package com.example.taste.domain.user.dto.request;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationSettingRequestDto {
	private NotificationCategory notificationCategory;
}

package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationRequestDto {
	@NotNull
	private NotificationCategory category;
	@NotBlank
	private String contents;
	private String redirectUrl;
	@Positive
	private Long redirectEntityId;
	private Long targetUserId;
}

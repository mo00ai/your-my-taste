package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationRequestDto {
	@NotNull
	@Pattern(regexp = "^(SYSTEM|MARKETING)$", message = "SYSTEM 혹은 MARKETING 알림만 전송 가능합니다.")
	private NotificationCategory category;
	@NotBlank
	private String contents;
	private String redirectUrl;
	private Long redirectEntityId;
	private Long targetUserId;
}

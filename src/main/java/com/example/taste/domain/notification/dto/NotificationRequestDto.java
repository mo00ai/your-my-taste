package com.example.taste.domain.notification.dto;

import com.example.taste.domain.notification.NotificationCategory;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
	private NotificationCategory category;
	@NotBlank
	private String contents;
	private String redirectUrl;
	private Long targetUserId;
}

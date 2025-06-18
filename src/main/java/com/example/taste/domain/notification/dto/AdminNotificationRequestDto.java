package com.example.taste.domain.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationRequestDto {
	@NotNull
	@Pattern(regexp = "^(SYSTEM|MARKETING)$", message = "SYSTEM 혹은 MARKETING 알림만 전송 가능합니다.")
	private String category;
	@NotBlank
	private String contents;
	private String redirectUrl;
	@Positive
	private Long redirectEntityId;
	private Long targetUserId;
}

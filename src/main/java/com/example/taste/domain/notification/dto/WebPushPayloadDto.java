package com.example.taste.domain.notification.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WebPushPayloadDto {
	private NotificationCategory category;
	private String redirectUrl;
	private String content;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;
	private Long contentId;
}

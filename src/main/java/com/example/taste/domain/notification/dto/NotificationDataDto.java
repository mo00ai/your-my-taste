package com.example.taste.domain.notification.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.taste.domain.notification.entity.enums.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonSerialize
public class NotificationDataDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private Long userId;
	private NotificationCategory category;
	private String contents;
	private String redirectionUrl;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;
	private boolean read;

	@Builder
	public NotificationDataDto(Long userId, NotificationCategory category, String contents, String redirectionUrl,
		LocalDateTime createdAt, boolean read) {
		this.userId = userId;
		this.category = category;
		this.contents = contents;
		this.redirectionUrl = redirectionUrl;
		this.createdAt = createdAt;
		this.read = read;
	}

	public void buildUrl(String url, Long entity) {
		if (url == null || url.isBlank()) {
			return;
		}
		this.redirectionUrl = url;
		if (entity != null) {
			this.redirectionUrl += "/" + entity;
		}
	}

	public void readIt() {
		this.read = true;
	}

}

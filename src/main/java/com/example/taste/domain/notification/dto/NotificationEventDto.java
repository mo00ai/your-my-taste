package com.example.taste.domain.notification.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.taste.domain.notification.NotificationCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonSerialize
@AllArgsConstructor
public class NotificationEventDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private NotificationCategory category;
	private String content;
	private String redirectUrl;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;
	private boolean read;

	// 유저 타겟 알림의 경우 알림을 보낼 유저
	// 구독자 위한 알림인 경우 구독대상 유저(이 유저의 구독자들을 찾아서 알림을 보냄)
	private Long userId;

	// 저장시에 부여되는 id
	private Long contentId;

	public void readIt() {
		this.read = true;
	}

	@Builder
	public NotificationEventDto(NotificationCategory category, String content, String redirectUrl,
		LocalDateTime createdAt,
		boolean read, Long userId) {
		this.category = category;
		this.content = content;
		this.redirectUrl = redirectUrl;
		this.createdAt = createdAt;
		this.read = read;
		this.userId = userId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}
}

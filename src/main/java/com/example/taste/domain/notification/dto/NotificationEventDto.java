package com.example.taste.domain.notification.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.example.taste.domain.notification.NotificationCategory;
import com.example.taste.domain.notification.NotificationType;
import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonSerialize
public class NotificationEventDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	private User user;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private NotificationCategory category;
	private NotificationType type;
	private Long redirectionEntity;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt;
	private boolean read;
	private String additionalText;

	@Builder
	public NotificationEventDto(User user, NotificationCategory category, NotificationType type, Long redirectionEntity,
		LocalDateTime createdAt, Long contentId, String additionalText) {
		this.user = user;
		this.category = category;
		this.type = type;
		this.redirectionEntity = redirectionEntity;
		this.createdAt = createdAt;
		this.read = false;
		this.contentId = contentId;
		this.additionalText = additionalText;
	}

	// 저장시에 부여되는 id
	@Setter
	private Long contentId;

	public void readIt() {
		this.read = true;
	}

}
